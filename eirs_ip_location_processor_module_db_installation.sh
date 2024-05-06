#!/opt/homebrew/bin/bash
conffile=/Users/dexter/eirsapp/configuration/configuration.properties
typeset -A config # init array

while read line
do
    if echo $line | grep -F = &>/dev/null
    then
        varname=$(echo "$line" | cut -d '=' -f 1)
        config[$varname]=$(echo "$line" | cut -d '=' -f 2-)
    fi
done < $conffile
conn1="mysql -h${config[ip]} -P${config[dbPort]} -u${config[dbUsername]} -p${config[dbPassword]}"
conn="mysql -h${config[ip]} -P${config[dbPort]} -u${config[dbUsername]} -p${config[dbPassword]} ${config[appdbName]}"

echo "creating apptest database."
${conn1} -e "CREATE DATABASE IF NOT EXISTS apptest;"
echo "apptest database successfully created!"

`${conn} <<EOFMYSQL


CREATE TABLE if not exists ip_location_country_ipv4 (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  start_ip_number int unsigned DEFAULT NULL,
  end_ip_number int unsigned NOT NULL,
  country_code char(2) DEFAULT NULL,
  country_name varchar(64) DEFAULT NULL,
  data_source varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE if not exists ip_location_country_ipv6 (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  start_ip_number decimal(39,0) unsigned DEFAULT NULL,
  end_ip_number decimal(39,0) unsigned NOT NULL,
  country_code char(2) DEFAULT NULL,
  country_name varchar(64) DEFAULT NULL,
  data_source varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE if not exists sys_param (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  description varchar(255) DEFAULT '',
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  tag varchar(255) DEFAULT NULL,
  type int DEFAULT '0',
  value text,
  active int DEFAULT '0',
  feature_name varchar(255) DEFAULT '',
  remark varchar(255) DEFAULT '',
  user_type varchar(255) DEFAULT '',
  modified_by varchar(255) DEFAULT '',
  PRIMARY KEY (id),
  UNIQUE KEY tag (tag)
);

CREATE TABLE if not exists sys_generated_alert (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  alert_id varchar(20) DEFAULT '',
  description varchar(250) DEFAULT '',
  status int DEFAULT '0',
  user_id int DEFAULT '0',
  username varchar(50) DEFAULT '',
  PRIMARY KEY (id)
);

CREATE TABLE if not exists cfg_feature_alert (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  alert_id varchar(20) DEFAULT '',
  description varchar(250) DEFAULT '',
  feature varchar(250) DEFAULT '',
  PRIMARY KEY (id)
);

insert into sys_param (description, tag, value, feature_name) SELECT 'The URL used to download IP location dump.', 'ipLocationDumpFileURL', 'https://www.ip2location.com/download?token=<token>\&file=<code>', 'Ip Location Processor' FROM dual WHERE NOT EXISTS ( SELECT * FROM sys_param WHERE tag = 'ipLocationDumpFileURL');
insert into sys_param (description, tag, value, feature_name) SELECT 'The code used to download ipv4 ip location dump.', 'ipLocationCodeipv4', 'DB1', 'Ip Location Processor' FROM dual WHERE NOT EXISTS ( SELECT * FROM sys_param WHERE tag = 'ipLocationCodeipv4');
insert into sys_param (description, tag, value, feature_name) SELECT 'The code used to download ipv6 ip location dump', 'ipLocationCodeipv6', 'DB1IPV6', 'Ip Location Processor' FROM dual WHERE NOT EXISTS ( SELECT * FROM sys_param WHERE tag = 'ipLocationCodeipv6');
insert into sys_param (description, tag, value, feature_name) SELECT 'The tag is used to store the last processed date for ip location processor for ip-type ipv6.', 'last_process_date_ip_location_ipv6', '', 'Ip Location Processor' FROM dual WHERE NOT EXISTS ( SELECT * FROM sys_param WHERE tag = 'last_process_date_ip_location_ipv6');
insert into sys_param (description, tag, value, feature_name) SELECT 'The tag is used to store the last processed date for ip location processor for ip-type ipv4.', 'last_process_date_ip_location_ipv4', '', 'Ip Location Processor' FROM dual WHERE NOT EXISTS ( SELECT * FROM sys_param WHERE tag = 'last_process_date_ip_location_ipv4');
insert into sys_param (description, tag, value, feature_name) SELECT 'The tag is used to store the token for downloading the ip location dump files.', 'ipLocationURLKey', 'RBq0UtKeLBmZrQeaLwBLjTzhGTVtqqzvjp7idqG4UYNMGgKcSCeNCwvIFHUnjP4d', 'Ip Location Processor' FROM dual WHERE NOT EXISTS ( SELECT * FROM sys_param WHERE tag = 'ipLocationURLKey');

insert into cfg_feature_alert (alert_id, description, feature) values ('alert2142', 'The DB configuration is missing.', 'Ip Location Processor');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert2143', 'The values for either IP Location Processor dump file url or IP Location Processor url key is missing in database <e>', 'Ip Location Processor');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert2144', 'The file downloading failed for  <e>.', 'Ip Location Processor');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert2145', 'The file downloading was incomplete for <e>.', 'Ip Location Processor');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert2146', 'The dump file is not found for <e>.', 'Ip Location Processor');
insert into cfg_feature_alert (alert_id, description, feature) values ('alert2147', 'The java process did not complete successfully for file <e> for <process_name>.', 'Ip Location Processor');

EOFMYSQL`

echo "creating aud database."
${conn1} -e "CREATE DATABASE IF NOT EXISTS aud;"
echo "aud database successfully created!"

conn2="mysql -h${config[ip]} -P${config[dbPort]} -u${config[dbUsername]} -p${config[dbPassword]} ${config[auddbName]}"

`${conn2} << EOFMYSQL


CREATE TABLE if not exists modules_audit_trail (
  id int NOT NULL AUTO_INCREMENT,
  created_on timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  modified_on timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  execution_time int DEFAULT '0',
  status_code int DEFAULT '0',
  status varchar(100) DEFAULT NULL,
  error_message varchar(255) DEFAULT NULL,
  module_name varchar(50) DEFAULT '',
  feature_name varchar(50) DEFAULT '',
  action varchar(20) DEFAULT '',
  count int DEFAULT '0',
  info varchar(255) DEFAULT '',
  server_name varchar(30) DEFAULT '',
  count2 int DEFAULT '0',
  failure_count int DEFAULT '0',
  PRIMARY KEY (id)
);
alter table modules_audit_trail modify error_message varchar(1000);
EOFMYSQL`
echo "tables creation completed."
echo "                                             *
						  ***
						 *****
						  ***
						   *                           "
echo "********************Thank You DB Process is completed now for IP Location Processor Module*****************"
