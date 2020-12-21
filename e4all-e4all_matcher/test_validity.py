from util.persistence import MongoDB
from models.models import Demand, Supply, EnergyTransaction

db = MongoDB()

### Check all combinations exhausted ###

demand = db.get_collection(Demand, filter={"remaining": {"$gt": 0}})

invalid_cases = 0
for position in demand:
    supply = db.get_collection(Supply, filter={"remaining": {"$gte": position.remaining}})
    if len(supply) > 0:
        invalid_cases += 1

if invalid_cases == 0:
    print("All combinations of supply and demand are exhausted.")
else:
    print("""Not all combinations of supply and demand were exhausted.
        {} Cases remaining.""".format(invalid_cases))

### Check every match has a transaction ###

demand = db.get_collection(Demand, filter={"remaining": 0})
transactions = db.get_collection(EnergyTransaction)

if len(demand) == len(transactions):
    print("Every match has a transaction.")
else:
    difference = demand - transactions
    print("Not every match has a transaction. Missing {} transactions.".format(difference))