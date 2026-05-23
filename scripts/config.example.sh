#!/usr/bin/env bash

# ========================================
# CONFIG TEMPLATE - Copy to config.sh and modify
# ========================================

# ==== YOUR TOMCAT PATH ====
export CATALINA_HOME="/path/to/apache-tomcat-11.0.7"
export TOMCAT_HOME="${CATALINA_HOME}"

# ==== DATA DIRECTORY ====
# Sets the data directory path (defaults to Tomcat's data folder)
export TA_HIRING_DATA_DIR="${CATALINA_HOME}/data"

# ==== APP NAME (optional) ====
export APP_NAME="groupproject"