#!/bin/bash

export DATABASE_URL=`cat /secrets/db-admin/jdbcUrl`
export DATABASE_TYPE=OPENTSDB
export DATABASE_DRIVER="org.postgresql.Driver"
export DATATYPE_STANDARD="CWMS"
export KEYGENERATOR="decodes.sql.SequenceKeyGenerator"

source /opt/opendcs/tsdb_config.sh
echo "***** GENERATED PROPERTIES FILE *****"
cat /dcs_user_dir/user.properties
echo "***** END GENERATED PROPERTIES FILE *****"

# TODO: Get all "placeholder. envvars and strip the placeholder. off and make list
# to Apply to below command.

PLACEHOLDERS=()
unset IFS
for var in $(compgen -e); do
    name=$var
    value=${!var}
    if [[ "$name" =~ ^placeholder_.*$ ]]; then
        PLACEHOLDERS+=("-D${name/placeholder_/}=${value}")
    fi
    
done
echo "Placeholders ${PLACEHOLDERS[@]}"
exec /opt/opendcs/bin/manageDatabase -I OpenDCS-Postgres \
               -P /dcs_user_dir/user.properties \
               -username "${DCS_OWNER}" \
               -password "${DCS_OWNER_PASS}" \
               -appUsername "${DCS_USER}" \
               -appPassword "${DCS_PASS}" \
               "${PLACEHOLDERS[@]}"