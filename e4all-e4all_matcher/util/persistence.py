from pymongo import MongoClient
from bson.objectid import ObjectId
import json

DATABASE = "E4all"
HOST = "localhost"
PORT = 27017

class MongoDB(object):
    def __init__(self):
        self.client = MongoClient(HOST, PORT)
        self.db = self.client[DATABASE]


    def insert(self, instance):
        klass = instance.__class__.__name__.lower()
        document = instance.__dict__
        _id = self.db[klass].insert_one(document)
        instance._id = _id.inserted_id


    def get_collection(self, klass, filter={}, sort=None):
        if "_id" in filter.keys():
            filter["_id"] = self._convert_instance_id(filter["_id"])

        collection = self.db[klass.__name__.lower()].find(filter)
        if sort != None:
            collection = collection.sort(sort)

        deserialized = []
        
        for element in collection:
            instance = self.deserialize(klass, element)
            deserialized.append(instance)

        return deserialized


    def get_instance(self, klass, instance_id):
        instance_id = self._convert_instance_id(instance_id)
        instance = self.db[klass.__name__.lower()].find_one({"_id": instance_id})
        return self.deserialize(klass, instance)


    def update(self, instance):
        klass = instance.__class__.__name__.lower()
        document = instance.__dict__
        self.db[klass].update_one({'_id': instance._id}, {"$set": document}, upsert=False)


    def deserialize(self, klass, serialized):
        if serialized:
            instance = klass()
            instance.__dict__ = serialized
            return instance


    def _convert_instance_id(self, instance_id):
        if type(instance_id) is not ObjectId:
            instance_id = ObjectId(instance_id)
        return instance_id