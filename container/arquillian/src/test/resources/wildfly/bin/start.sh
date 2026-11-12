/opt/jboss/wildfly/bin/add-user.sh --realm ManagementRealm --user admin --password admin
/opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 -Djboss.http.port=$HTTP_PORT -Djboss.management.https.port=$MANAGEMENT_PORT
