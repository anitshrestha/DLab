#!/usr/bin/python

# *****************************************************************************
#
# Copyright (c) 2016, EPAM SYSTEMS INC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ******************************************************************************
import logging
import json
import sys
from dlab.fab import *
from dlab.meta_lib import *
from dlab.actions_lib import *
import os
import uuid


# Main function for provisioning notebook server
def run():
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    notebook_config = dict()
    notebook_config['uuid'] = str(uuid.uuid4())[:5]

    try:
        params = "--uuid {}".format(notebook_config['uuid'])
        local("~/scripts/{}.py {}".format('common_prepare_notebook', params))
    except Exception as err:
        traceback.print_exc()
        append_result("Failed preparing Notebook node.", str(err))
        sys.exit(1)

    try:
        params = "--uuid {}".format(notebook_config['uuid'])
        local("~/scripts/{}.py {}".format('zeppelin_configure', params))
    except Exception as err:
        traceback.print_exc()
        append_result("Failed configuring Notebook node.", str(err))
        sys.exit(1)


# Main function for terminating exploratory environment
def terminate():
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)
    try:
        local("~/scripts/{}.py".format('common_terminate_notebook'))
    except Exception as err:
        traceback.print_exc()
        append_result("Failed terminating Notebook node.", str(err))
        sys.exit(1)


# Main function for stopping notebook server
def stop():
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] +  "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)
    try:
        local("~/scripts/{}.py".format('common_stop_notebook'))
    except Exception as err:
        traceback.print_exc()
        append_result("Failed stopping Notebook node.", str(err))
        sys.exit(1)


# Main function for starting notebook server
def start():
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] +  "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    try:
        local("~/scripts/{}.py".format('common_start_notebook'))
    except Exception as err:
        traceback.print_exc()
        append_result("Failed starting Notebook node.", str(err))
        sys.exit(1)


# Main function for configuring notebook server after deploying EMR
def configure():
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'], os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] +  "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    try:
        local("~/scripts/{}.py".format('common_configure_analytic_tool'))
    except Exception as err:
        traceback.print_exc()
        append_result("Failed configuring analytical tool on Notebook node.", str(err))
        sys.exit(1)


# Main function for installing additional libraries for notebook
def install_libs():
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'],
                                               os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    try:
        local("~/scripts/{}.py".format('common_install_libs'))
    except Exception as err:
        traceback.print_exc()
        append_result("Failed installing additional libs for Notebook node. Exception: " + str(err))
        sys.exit(1)


# Main function for get available libraries for notebook
def list_libs():
    local_log_filename = "{}_{}_{}.log".format(os.environ['conf_resource'], os.environ['edge_user_name'],
                                               os.environ['request_id'])
    local_log_filepath = "/logs/" + os.environ['conf_resource'] + "/" + local_log_filename
    logging.basicConfig(format='%(levelname)-8s [%(asctime)s]  %(message)s',
                        level=logging.DEBUG,
                        filename=local_log_filepath)

    try:
        local("~/scripts/{}.py".format('common_list_libs'))
    except Exception as err:
        traceback.print_exc()
        append_result("Failed get available libraries for notebook node. Exception: " + str(err))
        sys.exit(1)