# Admin ACL Script
A script that change the admin ACL of an entity

# To run
run with arguments: <local/staging/prod> <synapseAdminUsername> <apiKey> <filePath>

# Create input file
Run this command in the prod database

select distinct acl.OWNER_ID
from ACL acl, JDORESOURCEACCESS p, JDORESOURCEACCESS_ACCESSTYPE at, JDONODE n
where acl.OWNER_TYPE='ENTITY' and n.id=acl.owner_id and
p.owner_id=acl.id and at.ID_OID=p.id and at.STRING_ELE='CHANGE_PERMISSIONS';

Export the output of this command to an csv file - the filePath parameter points to this file

NOTE: Remember to remove header of the csv file.
