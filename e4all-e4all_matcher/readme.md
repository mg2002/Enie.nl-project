E4All Demand and Supply Matcher
===============================

Setting up de E4All matcher
---------------------------

In order to set up the E4All matching software, the following steps need to be taken. Steps marked **Optional** are optional and are only relevant in case it's prefered to run the matcher in a virtual environment. This is encouraged as the virtual environment ensures that the correct runtime environment is available and dependecies do not interfere with other Python projects.

* make sure you have Python 2.7.x. installed.
* [install pip](http://pip.readthedocs.org/en/latest/installing.html) for automatically installing dependencies.
* Make sure you have virtualenv installed (`pip install virtualenv`) (**Optional**)
* run `virtualenv --python=python2.7 venv` (**Optional**)
* run `source venv/bin/activate` (**Optional**)
* run `pip install -r requirements.txt`
* run `sudo apt install mongodb-server`


Running the E4All matcher
-------------------------

In order to run the matching software, the following steps need to be taken. Manually starting the MongoDB server is not always necessary, as it is run automatically as a service while booting the system. However, in cases where the database service is not running, the first command may be used.

* execute `sudo mongod`
* execute `source venv/bin/activate`
* execute `FLASK_APP=api.py flask run`

After executing the above commands, [the API](http://localhost:5000) is ready to receive requests.


Using the E4All matcher
-----------------------

The E4All matcher exposes the following URL's:

* (Dashboard) [http://localhost:5000/](http://localhost:5000/)
* (Demand index) [http://localhost:5000/demand](http://localhost:5000/demand)
* (Supply index) [http://localhost:5000/supply](http://localhost:5000/supply)
* (Transactions index) [http://localhost:5000/transactions](http://localhost:5000/transactions)

For more information on how to use the matching software see the appendix of the E4All matcher documentation. The documentation contains both a directory structure overview as well as a full description of the REST-api.