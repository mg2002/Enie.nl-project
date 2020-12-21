from flask import Flask, request, json, render_template
from threading import Lock
from util.persistence import MongoDB
from util.statistics import init_statistics, process_collection
from models.models import Demand, Supply, EnergyTransaction
from util.matcher import Matcher
from datetime import datetime
from util.constants import *
import validators


app = Flask(__name__, static_url_path='/static')
database = MongoDB()
matcher = Matcher(database)
mutex = Lock()


### Dashboard ###


@app.route("/")
def index():
    demand_collection = database.get_collection(Demand)
    supply_collection = database.get_collection(Supply)
    transaction_collection = database.get_collection(EnergyTransaction)
    
    statistics = init_statistics()
    process_collection(demand_collection, statistics)
    process_collection(supply_collection, statistics)
    process_collection(transaction_collection, statistics)

    return render_template('dashboard.html', 
        demand_collection = demand_collection,
        supply_collection = supply_collection,
        demand_count      = len(demand_collection),
        supply_count      = len(supply_collection),
        transaction_count = len(transaction_collection),
        statistics        = statistics)


### Demand Routes ###


@app.route("/demand", methods=["POST"])
def demand_create():
    data = json.loads(request.data)

    try:
        validate_optional("match_callback_url", data, validators.url)
        
        demand = Demand(
            timeslot_id  = int(data["timeslot_id"]),
            amount       = float(data["amount"]),
            energy_type  = ENERGY_TYPES[data["energy_type"]],
            start        = str(datetime.strptime(data["start"], DATETIME_FORMAT)),
            duration     = int(data["duration"]),
            residence_id = int(data["residence_id"]),
            match_callback_url = data["match_callback_url"])
    except Exception as e:
        return "{}: {}".format(str(e.__class__.__name__), str(e)), 400  
    
    with mutex:
        database.insert(demand)
        transaction = matcher.match_demand(demand)

    response = {"demand_id": str(demand._id)}
    if transaction != None: response["transaction_id"] = str(transaction._id)

    return json.dumps(response)


@app.route("/demand/<demand_id>")
def demand_show(demand_id):
    demand = database.get_instance(Demand, demand_id)
    if demand == None:
        return "Demand with id: {} not found.".format(demand_id), 404

    transactions = database.get_collection(EnergyTransaction, filter={"demand_id": demand._id})
    
    if is_browserless_call(request):
        return json_response(demand, transactions=transactions)

    return render_template('demand.html', 
        demand            = demand,
        transaction_count = len(transactions),
        transactions      = transactions)


@app.route("/demand")
def demand_index():
    demand_collection = database.get_collection(Demand)
    
    statistics = init_statistics()
    process_collection(demand_collection, statistics)

    return render_template('demand_index.html', 
        collection = demand_collection,
        count      = len(demand_collection),
        statistics = statistics)


### Supply Routes ###


@app.route("/supply", methods=["POST"])
def supply_create():
    data = json.loads(request.data)
    
    try:
        validate_optional("match_callback_url", data, validators.url)
        
        supply = Supply(
            timeslot_id = int(data["timeslot_id"]),
            amount      = float(data["amount"]),
            energy_type = ENERGY_TYPES[data["energy_type"]],
            start       = str(datetime.strptime(data["start"], DATETIME_FORMAT)),
            duration    = int(data["duration"]),
            residence_id = int(data["residence_id"]),
            match_callback_url = data["match_callback_url"],
            price       = float(data["price"]))
    except Exception as e:
        return "{}: {}".format(str(e.__class__.__name__), str(e)), 400
    
    with mutex:
        database.insert(supply)
        transaction = matcher.match_supply(supply)
    
    response = {"supply_id": str(supply._id)}
    if transaction != None: response["transaction_id"] = str(transaction._id)

    return json.dumps(response)


@app.route("/supply/<supply_id>")
def supply_show(supply_id):
    supply = database.get_instance(Supply, supply_id)
    if supply == None:
        return "Supply with id: {} not found.".format(supply_id), 404

    transactions = database.get_collection(EnergyTransaction, filter={"supply_id": supply._id})
    
    if is_browserless_call(request):
        return json_response(supply, transactions=transactions)

    return render_template('supply.html', 
        supply            = supply,
        transaction_count = len(transactions),
        transactions      = transactions)


@app.route("/supply")
def supply_index():
    supply_collection = database.get_collection(Supply)
    
    statistics = init_statistics()
    process_collection(supply_collection, statistics)

    return render_template('supply_index.html',
        collection = supply_collection,
        count      = len(supply_collection),
        statistics = statistics)


### Transaction Routes ###


@app.route("/transactions")
def transaction_index():
    transaction_collection = database.get_collection(EnergyTransaction)
    
    statistics = init_statistics()
    process_collection(transaction_collection, statistics)

    return render_template('transaction_index.html',
        collection = transaction_collection,
        count      = len(transaction_collection),
        statistics = statistics)


@app.route("/transactions/<transaction_id>")
def transaction_show(transaction_id):
    transaction = database.get_instance(EnergyTransaction, transaction_id)
    
    if transaction == None:
        return "Transaction with id: {} not found.".format(transaction_id), 404

    if is_browserless_call(request):
        transaction.demand_id = str(transaction.demand_id)
        transaction.supply_id = str(transaction.supply_id)
        return json_response(transaction)

    return render_template('transaction.html', 
        transaction = transaction)


### Helper Functions ###


def is_browserless_call(request):
    if "Content-Type" in  request.headers.keys():
        if request.headers['Content-Type'] == 'application/json':
            return True
    return False


def json_response(instance, transactions=None):
    response = dict(instance.__dict__)
    response["_id"] = str(response["_id"])
    process_transactions(transactions, response)

    return json.dumps(response)


def process_transactions(transactions, response):
    if transactions:
        response["transactions"] = []
        
        for transaction in transactions:
            transaction_copy = dict(transaction.__dict__)
            transaction_copy["_id"] = str(transaction_copy["_id"])
            transaction_copy["demand_id"] = str(transaction_copy["demand_id"])
            transaction_copy["supply_id"] = str(transaction_copy["supply_id"])
            response["transactions"].append(transaction_copy)


def validate(key, data, validator):
        valid = validator(data[key])
        if not valid:
            raise ValueError("Invalid {} {}".format(valid.func.__name__, valid.value))


def validate_optional(key, data, validator):
    if key in data.keys():
        validate(key, data, validator)
    else:
        data[key] = None