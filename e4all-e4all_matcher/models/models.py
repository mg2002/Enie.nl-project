class EnergyPosition(object):
    def __init__(self, timeslot_id=None, amount=None, energy_type=None, start=None, duration=None, residence_id=None, match_callback_url=None):
        self.timeslot_id = timeslot_id
        self.amount = amount
        self.energy_type = energy_type
        self.start = start
        self.duration = duration
        self.residence_id = residence_id
        self.match_callback_url = match_callback_url


class Supply(EnergyPosition):
    def __init__(self, timeslot_id=None, amount=None, energy_type=None, start=None, duration=None, residence_id=None, match_callback_url=None, price=None):
        super(Supply, self).__init__(timeslot_id, amount, energy_type, start, duration, residence_id, match_callback_url)
        self.price = price
        self.remaining = amount


class Demand(EnergyPosition):
    def __init__(self, timeslot_id=None, amount=None, energy_type=None, start=None, duration=None, residence_id=None, match_callback_url=None):
        super(Demand, self).__init__(timeslot_id, amount, energy_type, start, duration, residence_id, match_callback_url)
        self.satisfied = 0.0
        self.remaining = amount


class EnergyTransaction(object):
    def __init__(self, demand_id=None, supply_id=None, amount=None, timeslot_id=None):
        self.demand_id = demand_id
        self.supply_id = supply_id
        self.amount = amount
        self.timeslot_id = timeslot_id
