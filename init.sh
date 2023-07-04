
# Delete existing data
curl -X DELETE http://localhost:8081/clients
curl http://localhost:8082/accounts/delete-all

# Create client
curl -d '{"id": "1784e89b-7a3b-45ed-b2f2-6a562756a2e3", "fullName": "John Smith", "email": "john.smith@gmail.com" }' -H "Content-Type: application/json" -X POST http://localhost:8081/clients

# Create accounts for this client
curl -d '{"id":"86c2de0c-d330-4032-b476-c56682f434ea", "clientId": "1784e89b-7a3b-45ed-b2f2-6a562756a2e3", "number": "ACC00001", "balance": 0 }' -H "Content-Type: application/json" -X POST http://localhost:8082/accounts
curl -d '{"id":"ecafb910-0e3e-40b7-b304-6115b708606a", "clientId": "1784e89b-7a3b-45ed-b2f2-6a562756a2e3", "number": "ACC00002", "balance": 0 }' -H "Content-Type: application/json" -X POST http://localhost:8082/accounts
curl -d '{"id":"76be90c1-0d30-4d68-b4c2-b77bbf185f5b", "clientId": "1784e89b-7a3b-45ed-b2f2-6a562756a2e3", "number": "ACC00003", "balance": 0 }' -H "Content-Type: application/json" -X POST http://localhost:8082/accounts

# Update balance of account 2
# Commented by default.
# To be executed before deleting the client to fail the transaction
#curl -d '{"id":"ecafb910-0e3e-40b7-b304-6115b708606a", "clientId": "1784e89b-7a3b-45ed-b2f2-6a562756a2e3", "number": "ACC00002", "balance": 100 }' -H "Content-Type: application/json" -X PUT http://localhost:8082/accounts