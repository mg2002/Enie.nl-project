import numpy as np
from models.models import EnergyTransaction, Supply, Demand
from http.client import HTTPConnection
from urllib.parse import urlparse

FIRST_FIT = 0
BEST_FIT = 1
WORST_FIT = 2

ALLOCATION_STRATEGIE = BEST_FIT


class Matcher(object):
    def __init__(self, database):
        self.database = database


    def match_demand(self, demand):
        if ALLOCATION_STRATEGIE == FIRST_FIT:
            return self.allocate_demand_first_fit(demand)
        elif ALLOCATION_STRATEGIE == BEST_FIT:
            return self.allocate_demand_best_fit(demand)
        else:
            return self.allocate_demand_worst_fit(demand)


    def match_supply(self, supply):
        if ALLOCATION_STRATEGIE == FIRST_FIT:
            return self.allocate_supply_first_fit(supply)
        elif ALLOCATION_STRATEGIE == BEST_FIT:
            return self.allocate_supply_best_fit(supply)
        else:
            return self.allocate_supply_worst_fit(supply)


    def allocate(self, demand_position, supply_position):
        demand_position.satisfied = demand_position.amount
        demand_position.remaining -= demand_position.amount
        supply_position.remaining -= demand_position.amount
        transaction = EnergyTransaction(demand_position._id, supply_position._id,
            demand_position.amount, demand_position.timeslot_id)
        
        # TODO: should be an atomic operation
        self.database.insert(transaction)
        self.database.update(demand_position)
        self.database.update(supply_position)

        self.send_callback_request(demand_position.match_callback_url)
        self.send_callback_request(supply_position.match_callback_url)

        if supply_position.remaining < 0:
            print(transaction.__dict__)

        return transaction


    def allocate_demand_first_fit(self, demand):
        supply = self.database.get_collection(Supply,
            filter={
                "remaining": {"$gte": demand.amount},
                "timeslot_id": demand.timeslot_id
            })

        if len(supply) > 0:
            first_fit = supply[0]
            return self.allocate(demand, first_fit)


    def allocate_demand_best_fit(self, demand):
        supply = self.database.get_collection(Supply,
            filter={
                "remaining": {"$gte": demand.amount},
                "timeslot_id": demand.timeslot_id
            },
            sort=[("remaining", -1)])
        
        if len(supply) > 0:
            best_fit = supply[0]
            return self.allocate(demand, best_fit)


    def allocate_demand_worst_fit(self, demand):
        supply = self.database.get_collection(Supply,
            filter={
                "remaining": {"$gte": demand.amount},
                "timeslot_id": demand.timeslot_id
            },
            sort=[("remaining", 1)])
        
        if len(supply) > 0:
            best_fit = supply[0]
            return self.allocate(demand, best_fit)


    def allocate_supply_first_fit(self, supply):
        demand = self.database.get_collection(Demand,
            filter={
                "$and": [
                    {"remaining": {"$lte": supply.remaining}},
                    {"remaining": {"$gt": 0}},
                ],
                "timeslot_id": supply.timeslot_id
            })

        if len(demand) > 0:
            first_fit = demand[0]
            return self.allocate(first_fit, supply)


    def allocate_supply_best_fit(self, supply):
        demand = self.database.get_collection(Demand,
            filter={
                "$and": [
                    {"remaining": {"$lte": supply.remaining}},
                    {"remaining": {"$gt": 0}}
                ],
                "timeslot_id": supply.timeslot_id
            },
            sort=[("remaining", -1)])

        if len(demand) > 0:
            best_fit = demand[0]
            return self.allocate(best_fit, supply)


    def allocate_supply_worst_fit(self, supply):
        demand = self.database.get_collection(Demand,
            filter={
                "$and": [
                    {"remaining": {"$lte": supply.remaining}},
                    {"remaining": {"$gt": 0}}
                ],
                "timeslot_id": supply.timeslot_id
            },
            sort=[("remaining", 1)])

        if len(demand) > 0:
            best_fit = demand[0]
            return self.allocate(best_fit, supply)


    def send_callback_request(self, url):
        if url == None: return

        host = urlparse(url).hostname
        path = urlparse(url).path or "/"
        port = urlparse(url).port or 80

        connection = HTTPConnection(host, port)
        connection.request("GET", path)