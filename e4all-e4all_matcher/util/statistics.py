from models.models import Demand, Supply, EnergyTransaction
import numpy as np


class Statistic(object):
    def process(self, position):
        raise NotImplementedError


    def get_value(self):
        raise NotImplementedError


class AverageAmountStatistic(Statistic):
    def __init__(self):
        self.total_demand = 0.0
        self.total_supply = 0.0
        self.demand_count = 0
        self.supply_count = 0

    
    def process(self, position):
        if type(position) is Demand:
            self.total_demand += position.amount
            self.demand_count += 1
        elif type(position) is Supply:
            self.total_supply += position.amount
            self.supply_count += 1


    def get_value(self):
        return {"demand": self.total_demand / max(self.demand_count, 1),
            "supply": self.total_supply / max(self.supply_count, 1)}


class SupplyAndDemandRatioStatistic(Statistic):
    def __init__(self):
        self.total_demand = 0.0
        self.total_supply = 0.0


    def process(self, position):
        if type(position) is Demand:
            self.total_demand += position.amount
        elif type(position) is Supply:
            self.total_supply += position.amount


    def get_value(self):
        return {"ratio": 100.0 * self.total_supply / max(self.total_demand, 1),
            "total_demand": self.total_demand,
            "total_supply": self.total_supply}


class AllocationStatistic(Statistic):
    def __init__(self):
        self.demand_allocation = []
        self.supply_allocation = []
        self.total_allocated = 0.0


    def process(self, position):
        if type(position) is Demand:
            self.demand_allocation.append(position.satisfied * 1.0 / position.amount * 100.0)
        if type(position) is Supply:
            self.supply_allocation.append((position.amount - position.remaining * 1.0) / (position.amount) * 100.0)
        if type(position) is EnergyTransaction:
            self.total_allocated += position.amount


    def get_value(self):
        average_demand = 0
        std_demand = 0
        average_supply = 0
        std_supply = 0

        if len(self.demand_allocation) > 0:
            average_demand = np.average(self.demand_allocation)
            std_demand = np.std(self.demand_allocation)
        if len(self.supply_allocation) > 0:
            average_supply = np.average(self.supply_allocation)
            std_supply = np.std(self.supply_allocation)

        return {"average_allocated_demand": (average_demand, std_demand),
            "average_allocated_supply": (average_supply, std_supply),
            "total_allocated": self.total_allocated}


class MinMaxStatistic(Statistic):
    def __init__(self):
        self.min_demand = float("inf")
        self.max_demand = 0
        self.min_supply = float("inf")
        self.max_supply = 0
        self.min_allocated = float("inf")
        self.max_allocated = 0


    def process(self, position):
        if type(position) is Demand:
            if position.amount < self.min_demand:
                self.min_demand = position.amount
            elif position.amount > self.max_demand:
                self.max_demand = position.amount
        elif type(position) is Supply:
            if position.amount < self.min_supply:
                self.min_supply = position.amount
            elif position.amount > self.max_supply:
                self.max_supply = position.amount
        elif type(position) is EnergyTransaction:
            if position.amount < self.min_allocated:
                self.min_allocated = position.amount
            elif position.amount > self.max_allocated:
                self.max_allocated = position.amount

    def get_value(self):
        return {"min_demand": self.min_demand, 
            "max_demand": self.max_demand,
            "min_supply": self.min_supply,
            "max_supply": self.max_supply,
            "min_allocated": self.min_allocated,
            "max_allocated": self.max_allocated}


class TimeslotStatistic(Statistic):
    def __init__(self):
        self.demand = {
            "x": [], 
            "y": []
        }
        self.supply = {
            "x": [], 
            "y": []
        }
        self.total_allocated = {
            "x": [], 
            "y": []
        }


    def process(self, position):
        if position.timeslot_id not in self.demand["x"]:
            for collection in [self.demand, self.supply, self.total_allocated]:
                collection["x"].append(position.timeslot_id)
                collection["y"].append(0)

        if type(position) is Demand:
            self.demand["y"][self.demand["x"].index(position.timeslot_id)] += position.amount
        if type(position) is Supply:
            self.supply["y"][self.supply["x"].index(position.timeslot_id)] += position.amount
        if type(position) is EnergyTransaction:
            self.total_allocated["y"][self.total_allocated["x"].index(position.timeslot_id)] += position.amount


    def get_value(self):
        return {"demand": self.demand,
            "supply": self.supply,
            "total_allocated": self.total_allocated}


### HELPER FUNCTIONS ###


def init_statistics():
    statistics = {}

    STATISTICS_MODULES = [
        AverageAmountStatistic,
        SupplyAndDemandRatioStatistic,
        AllocationStatistic,
        MinMaxStatistic,
        TimeslotStatistic
    ]

    for module in STATISTICS_MODULES:
        statistic = module()
        label = statistic.__class__.__name__
        statistics[label] = statistic

    return statistics


def process_collection(collection, statistics):
    for element in collection:
        for statistic in statistics.values():
            statistic.process(element)
