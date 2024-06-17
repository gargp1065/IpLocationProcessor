#!/bin/bash


# function to log messages
  log_message() {
    # Get the current date and timestamp
    datetime=$(date +"%Y-%m-%d %H:%M:%S.%3N")
    # Get the line number of the caller
    lineno=$(caller | awk '{print $1}')
    # Print the log message with date, timestamp, and line number
    echo "$datetime [Line $lineno] $1"
  }

  # function to raise alert

  function generateAlert() {
    id=$1
    log_message "Raising alert for alert id $id"
    curlOutput=$(curl -s ""$curlUrl"/"$id"")
    if [ $? -ne 0 ]; then
      log_message "Error: Alert not raised due to some error."
    else
      log_message "Alert was raised successfully."
    fi
  }

  function generateAlertUsingUrl() {
    alertId=$1
    alertMessage=$2
    alertProcess=$3
    alertUrl=$4
    curlOutput=$(curl --header "Content-Type: application/json"   --request POST   --data '{"alertId":"'$alertId'",
    "alertMessage":"'"$alertMessage"'", "userId": "0", "alertProcess": "'"$alertProcess"'", "serverName": "'"$serverName"'",  "featureName": "Ip Location"}' "$alertUrl")
    echo $curlOutput
  }

  function updateAuditEntry() {
      errMsg=$1
      executionStartTime=$2
      moduleName=$3
      featureName=$4
      log_message "$errMsg" "$executionStartTime" "$moduleName" "$featureName"
    #  echo $executionStartTime
      executionFinishTime=$(date +%s.%N);
      executionTime=$(echo "$executionFinishTime - $executionStartTime" | bc)
      secondDivision=1000
      finalExecutionTime=`echo "$executionTime * $secondDivision" | bc`
    #  echo $finalExecutionTime
  #    echo $dbIp $dbPort $dbUsername $dbPassword $auddbName
      mysqlOutput=$(mysql -h$dbIp -P$dbPort -u$dbUsername -p${dbPassword} $auddbName -se "update modules_audit_trail
      set status_code='501',status='FAIL',error_message='$errMsg',
      execution_time='$finalExecutionTime',modified_on=CURRENT_TIMESTAMP where module_name='$moduleName'
      and feature_name='$featureName' order by id desc limit 1")
      log_message "Updating the modules_audit_trail entry for error $errMsg"
    }

    function checkFileUploadComplete() {
      fullFile=$1
      initialFileSize=$(wc -c <"$fullFile")
      sleep $initialTimer
      currentFileSize=$(wc -c <"$fullFile")
      log_message
      while [ $currentFileSize -ne $initialFileSize ]
      do
        log_message "File $fullFile is still downloading. Will check again in next $finalTimer seconds."
        initialFileSize=$currentFileSize
        sleep $finalTimer
      done
      log_message "File "$fullFile" downloading completed. Now will process the file for further steps."
      return 1;
    }

function fileCopy() {
      sourceFileName=$1
      sourceFilePath=$2
      count=$3
      IFS=',' read -ra destPaths <<< "$destinationPath"
      IFS=',' read -ra destServers <<< "$destinationServer"

      # Construct the JSON body string with dynamic array of destinations
      jsonBody=$(cat <<EOF
      {
        "appName": "ipLocationProcessor",
        "destination": [
EOF
      )

      # Iterate over each destination path and server name
      for ((i=0; i<${#destPaths[@]}; i++)); do
        # Construct the destination object
        destination="{\"destFilePath\": \"${destPaths[$i]}\", \"destServerName\": \"${destServers[$i]}\"}"
        # Add the destination object to the JSON body string
        jsonBody+="    $destination"
        if [ $i -lt $((${#destPaths[@]} - 1)) ]; then
          jsonBody+=","
        fi
        jsonBody+=$'\n' # Add newline for readability
      done

      # Complete the JSON body string
      jsonBody+="  ],
        \"remarks\": \"\",
        \"serverName\": \"$serverName\",
        \"sourceFileName\": \"$sourceFileName\",
        \"sourceFilePath\": \"$sourceFilePath\",
        \"sourceServerName\": \"$sourceServerName\",
        \"txnId\": \"\"
      }"

      # Send the request with curl

      response=$(curl -X POST \
        "$fileCopyApi" \
        -H 'Content-Type: application/json' \
        -d "$jsonBody" \
        2>/dev/null)

      message=$(echo "$response" | jq -r '.message')

      # Check if the message is "Success"
      if [ "$message" = "Success" ]; then
        log_message "The file copy request was successful"
      else
        log_message "The file copy request failed. Error message: $message"
        log_message "Making an entry in list_file_mgmt table"
        for ((i=0; i<${#destPaths[@]}; i++)); do
          # Construct the destination object
          mysql -h$dbIp -P$dbPort $appdbName -u$dbUsername -p${dbPassword} << EOFMYSQL
              insert into list_file_mgmt (created_on, modified_on, file_name, file_path, source_server, list_type, operator_name, file_type, file_state,
	      record_count, copy_status, destination_path, destination_server) values (NOW(), NOW(), "$sourceFileName", "$sourceFilePath",
              "$serverName", "OTHERS","ALL","1", 1, "$count", 0, "${destPaths[$i]}", "${destServers[$i]}");
EOFMYSQL
        done

      fi
    }
    source ~/.bash_profile
    . $1
    ipType=$2
    executionStartTime=$(date +%s.%N)
    log_message "The server host name is: $serverName"
    commonConfiguration=$commonConfigurationFilePath
    if [ ! -e "$commonConfiguration" ]
        then
          log_message "$commonConfiguration file not found ,the script is terminated."
          exit 1;
    fi
    source $commonConfiguration

    # Reading password from the config file.
    log_message "Retrieving password for database connection."
    dbPassword=$(java -jar $encryptorPath spring.datasource.password)

    if [ -z "$dbIp" ] || [ -z "$dbPort" ] || [ -z "$dbUsername" ] || [ -z "$dbPassword" ] ;
      then
        log_message "DB details missing, the script is terminated. Raising alert"
        generateAlertUsingUrl 'alert2142' '' '' $alertUrl
        exit 1;
    fi
    moduleName="IP Location"
    featureName="IP_Location_Manager_$ipType"
    previousStatus=$(mysql -h$dbIp -P$dbPort $auddbName -u$dbUsername  -p${dbPassword} -se "select status_code from modules_audit_trail where feature_name='$featureName' and module_name='$moduleName' and created_on like '%$(date +%F)%' order by id desc limit 1");
    log_message "The previous status of execution for ip-type '$ipType' is '$previousStatus'";

    if [ $previousStatus -eq 200 ];
      then
        log_message "The process for ip-type '$ipType' already completed for the day. Exiting from the script"
        exit 1;
    fi

    executionStartTime=$(date +%s.%N)
    ## making entry in modules_audit_trail for start of the process.

    mysql -h$dbIp -P$dbPort $auddbName -u$dbUsername -p${dbPassword} << EOFMYSQL
    insert into modules_audit_trail (status_code,status,error_message,feature_name,server_name,execution_time,module_name)
          values(201,'INITIAL','NA','$featureName','$serverName',0,'$moduleName');
EOFMYSQL

    currentDate=$(date +%F)
    fileNameToProcess="ip_location_country_$iptype_$currentDate.csv"
    lastProcessedDateTag="last_process_date_ip_location_$ipType"
    lastProcessedDate=$(mysql -h$dbIp -P$dbPort $appdbName -u$dbUsername -p${dbPassword} -se "select value from sys_param where tag='$lastProcessedDateTag'")
    log_message "The last processed date for $ipType is $lastProcessedDate"
    lastProcessedFileName="ip_location_country_$iptype_$lastProcessedDate.csv"
    # check if lastProcessedDate exists or not. If not exists this is the first time process is running
    fullProcessedFileName=$processedFilePath"/"$lastProcessedFileName
    log_message "The previous process file is $fullProcessedFileName"
    if [[ ! -z "$lastProcessedDate"  && ! -f "$fullProcessedFileName" ]];
      then
        log_message "The previous processed file does not exists on the server."
        updateAuditEntry "The previous processed file $lastProcessedFileName not found for $ipType." "$executionStartTime" "$moduleName" "$featureName"
        generateAlertUsingUrl "alert2148" "$ipType" "$lastProcessedFileName" "$alertUrl"
        exit 1

    fi

    ipLocationDumpFileUrl=$(mysql -h$dbIp -P$dbPort $appdbName -u$dbUsername -p${dbPassword} -se "select value from sys_param where tag='ipLocationDumpFileURL'")
    ipLocationUrlKey=$(mysql -h$dbIp -P$dbPort $appdbName -u$dbUsername -p${dbPassword} -se "select value from sys_param where tag='ipLocationURLKey'")
    ipLocationCode=$(mysql -h$dbIp -P$dbPort $appdbName -u$dbUsername -p${dbPassword} -se "select value from sys_param where tag='ipLocationCode$ipType'")
    if [ -z "$ipLocationDumpFileUrl" ] || [ -z "$ipLocationUrlKey" ] || [ -z "$ipLocationCode" ] ;
      then
        log_message "The values for either ip location dump file url or ip location url key is missing in database."
        updateAuditEntry 'The values for either ip location dump file url or ip location url key is missing in database.' "$executionStartTime" "$moduleName" "$featureName"
        generateAlertUsingUrl 'alert2143' $ipType '' $alertUrl
        log_message "Terminating the process."
        exit 3;
    fi
    log_message $ipLocationDumpFileUrl
    ipLocationDumpFileUrl="${ipLocationDumpFileUrl/<code>/$ipLocationCode}"
    ipLocationDumpFileUrl="${ipLocationDumpFileUrl/<token>\\/$ipLocationUrlKey}"
    log_message $ipLocationDumpFileUrl
    databaseZip="$inputFilePath/database.zip"
    log_message "The zip file is downloaded as $databaseZip"
    # now call the api to get the file.....
    wget -O $databaseZip -q $ipLocationDumpFileUrl 2>&1
#
    checkFileUploadComplete $databaseZip
#
    if [ ! -f $databaseZip ]; then
      log_message "The file downloading failed for $ipType"
      updateAuditEntry "The file downloading fa iled for $ipType" "$executionStartTime" "$moduleName" "$featureName"
      generateAlertUsingUrl "alert2144" $ipType '' $alertUrl
      exit 0
    fi
#
    if [ $(wc -c < $databaseZip) -lt $fileSize ]; then
      log_message "The file downloaded was incomplete."
      updateAuditEntry "The file downloading was incomplete for $ipType" "$executionStartTime" "$moduleName" "$featureName"
      generateAlertUsingUrl "alert2145" $ipType '' $alertUrl
      exit 0
    fi
    cd $inputFilePath
    unzip -q -o $databaseZip

    if [ -z "$(find . -name "*$ipFileName*")" ]; then
      log_message "The ip location dump file not found for $ipType."
      updateAuditEntry "The ip location dump file not found for $ipType.." "$executionStartTime" "$moduleName" "$featureName"
      generateAlertUsingUrl "alert2146" $ipType '' $alertUrl
      exit 0
    fi
    downloadedFileName="$(find . -name "*$ipFileName*")"
    log_message "Downloaded file is $downloadedFileName"
    dateFormat=$(date +%Y%m%d)
    fileName="ip_location_country_$ipType_$dateFormat.csv"
    fullFileName=$inputFilePath"/"$fileName
    mv $downloadedFileName $fullFileName
    totalRecords=$(wc -l <"$fullFileName")
    log_message "Total records in new file is $totalRecords"
    takeDiff=1
    if [ -z "$lastProcessedDate" ];
      then
        log_message "The last processed date not exists. This is the first time processing. Taking current file as first dump to process."
        takeDiff=0
    fi

    sortedTempFile="$inputFilePath/sortedFile.csv"
    log_message "Sorting the ip country file for creating diff files."
    sorted=$(sort "$fullFileName" > "$sortedTempFile")
    log_message "The sorted temp file created successfully."

    outputFileDeletion="$deltaFilePath/ip_location_country_del_"$ipType"_diff_$(date +%Y%m%d).csv"
    outputFileAddition="$deltaFilePath/ip_location_country_add_"$ipType"_diff_$(date +%Y%m%d).csv"
    > "$outputFileDeletion"
    > "$outputFileAddition"

    lastProcessedFileName="ip_location_country_$iptype_$lastProcessedDate.csv"
    log_message "Previous processed file name $lastProcessedFileName"

    if [ -f "$fullProcessedFileName" ];
      then
        # previous file exists take diff

        diffStartTime=$(date +%s%3N)
        #diff_output=$(diff "$processedFile" "$tempFile" | grep '>' | cut -c 3-)
        diffOutputDeletion=$(diff -B --changed-group-format='%<' --unchanged-group-format='' "$fullProcessedFileName" "$sortedTempFile")
        diffOutputAddition=$(diff -B --changed-group-format='%>' --unchanged-group-format='' "$fullProcessedFileName" "$sortedTempFile")
        #echo "$headers" > "$output_file"
        echo "$diffOutputDeletion" > "$outputFileDeletion"
        echo "$diffOutputAddition" >  "$outputFileAddition"
        diffEndTime=$(date +%s%3N)  # Get end time in milliseconds
        execution_time=$((diffEndTime - diffStartTime))
        log_message "Diff file creation execution time: $execution_time ms"
    fi
    if [ "$takeDiff" -eq "0" ];
      then
        log_message "This is the first time script is running."
         cp "$sortedTempFile" "$outputFileAddition"
      else
        log_message "The previous processed file does not exists on the server."
        updateAuditEntry "The previous processed file $lastProcessedFileName not found for $ipType." "$executionStartTime" "$moduleName" "$featureName"
        generateAlertUsingUrl "alert2148" $ipType '' $alertUrl
        exit 1
    fi

    if [ ! -s "$outputFileAddition" ] && [ ! -s "$outputFileDeletion" ];
      then
        log_message "The diff files are empty. Exiting the process."
        mysql -h$dbIp -P$dbPort -u$dbUsername -p${dbPassword} $auddbName << EOFMYSQL
              update modules_audit_trail set status_code=200, status='SUCCESS', info="$fileName", execution_time="$finalExecutionTime" where feature_name="$featureName" and module_name="$moduleName" order by id desc limit 1;
EOFMYSQL
        log_message "Move the $sortedTempFile to $processedFilePath"
        mv 	${sortedTempFile} ${processedFilePath}/${fileName}
        log_message "Remove the $fullFileName"
        rm $fullFileName
#        log_message "Moved file ${sortedTempFile} to ${processedFilePath}/${fileName}."
        mv $outputFileDeletion $deltaFileProcessedPath
        log_message "Moved file ${outputFileDeletion} to $deltaFileProcessedPath."
        mv $outputFileAddition $deltaFileProcessedPath
        log_message "Moved file $outputFileAddition to $deltaFileProcessedPath."
        log_message "Remove the remaining files from ${inputFilePath}}"
        rm $inputFilePath/*
        exit 0
    # now file is downloaded successfully. Need to check if previous processed file exists or not. If not
    fi
    log_message "Starting java code $javaFeatureName"
    cd $javaProcessPath
    java -Dlog4j.configurationFile=file:$javaProcessLogFile -jar ipLocationProcessor.jar --spring.config.location=$commonConfiguration,$javaProcessPropertyFile 1>/dev/null 2>/dev/null
    javaFeature="$javaFeatureName"_"$ipType"
    log_message $javaFeature
    fileProcessStatusCode=$(mysql -h$dbIp -P$dbPort $auddbName -u$dbUsername -p${dbPassword} -se "select status_code from modules_audit_trail where created_on LIKE '%$(date +%F)%' and feature_name='$javaFeature' and module_name='$moduleName'  order by id desc limit 1");
    log_message "The status code from processor after completion is: $fileProcessStatusCode"

    if [ "$fileProcessStatusCode" -eq 200 ] ;
      then
        log_message "The processor completed successfully"
        log_message "Move the $sortedTempFile to $processedFilePath"
        mv 	${sortedTempFile} ${processedFilePath}/${fileName}
        log_message "Remove the $fullFileName"
        rm $fullFileName
        mv $outputFileDeletion $deltaFileProcessedPath
        log_message "Moved file ${outputFileDeletion} to $deltaFileProcessedPath."
        mv $outputFileAddition $deltaFileProcessedPath
        log_message "Moved file $outputFileAddition to $deltaFileProcessedPath."
	log_message "Revove the remaining files from ${inputFilePath}}"
        rm $inputFilePath/*
#        log_message "Moved file ${sortedTempFile} to ${processedFilePath}/${fileName}."
        #mv $outputFileDeletion $deltaFileProcessedPath
        #log_message "Moved file ${outputFileDeletion} to $deltaFileProcessedPath."
        #mv $outputFileAddition $deltaFileProcessedPath
        #log_message "Moved file $outputFileAddition to $deltaFileProcessedPath."
        cd $fileScriptProcessPath
     else
        log_message "The status of processor execution is not equal to 200."
        executionFinishTime=$(date +%s.%N);
        ExecutionTime=$(echo "$executionFinishTime - $executionStartTime" | bc)
        secondDivision=1000
        finalExecutionTime=`echo "$ExecutionTime * $secondDivision" | bc`
        updateAuditEntry 'The java process did not complete successfully for file '$fileName'.' "$executionStartTime" "$moduleName" "$featureName"
        generateAlertUsingUrl 'alert2147' $fileName $ipType $alertUrl
        exit 1
    fi

     #8. Success entry in audit table.
fileCopy "$fileName" "$processedFilePath" "$totalRecords"
executionFinishTime=$(date +%s.%N);
  ExecutionTime=$(echo "$executionFinishTime - $executionStartTime" | bc)
  secondDivision=1000
  finalExecutionTime=`echo "$ExecutionTime * $secondDivision" | bc`

  log_message "Last processed date tag $lastProcessedDateTag has value $lastProcessedDate"
  mysql -h$dbIp -P$dbPort -u$dbUsername -p${dbPassword} $appdbName << EOFMYSQL
    update sys_param set value='$dateFormat' where tag='$lastProcessedDateTag';
EOFMYSQL
log_message "Last processed date tag $lastProcessedDateTag has updated value $dateFormat"
  mysql -h$dbIp -P$dbPort -u$dbUsername -p${dbPassword} $auddbName << EOFMYSQL
      update modules_audit_trail set status_code=200, status='SUCCESS', info='$fileName', execution_time="$finalExecutionTime", count='$totalRecords' where feature_name='$featureName' and module_name='$moduleName' order by id desc limit 1;
EOFMYSQL

    log_message "IP Location process for ip-type $ipType is completed successfully."
    exit 0;