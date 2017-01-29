var db = connect('127.0.0.1:27017/BookingsServiceDB');
print('* Connected to database');

db.dropDatabase();

db.Services.drop();
db.createCollection("Services", {
    validator: {
        $and: [{
            name: {
                $type: "string",
                $exists: true
            },
            price: {
                $type: "int",
                $exists: true
            },
            description: {
                $type: "string",
                $exists: true
            },
            createdDate: {
                $type: "string",
                $exists: true
            }
        }]
    },
    validationAction: "error"
});
db.Services.createIndex(
    {name: 1},
    {unique: true}
);
print('* Created collection Services. All collections: ' + db.getCollectionNames());

db.dropRole("ServicesDatabaseProxyRole");
db.createRole({
    role: "ServicesDatabaseProxyRole",
    privileges: [{
        resource: {
            db: "BookingsServiceDB",
            collection: "Services"
        },
        actions: [ "find", "update", "insert", "remove" ]
    }],
    roles: []
});
print('* Created role ServicesDatabaseProxyRole');

db.dropUser("ServicesDatabaseProxyUser");
db.createUser({
    user: "ServicesDatabaseProxyUser",
    pwd: "BookingsServicep@Ssw0rd",
    roles: [{
        role: "ServicesDatabaseProxyRole",
        db: "BookingsServiceDB"
    }]
});
print('* Created user ServicesDatabaseProxyUser');

db.Services.insert({
    name: "usluga1",
    price: NumberInt(15),
    createdDate: "2017-01-02",
    description: "opis uslugi 1"
});

db.Services.insert({
    name: "usluga2",
    price: NumberInt(15),
    createdDate: "2017-01-02",
    description: "opis uslugi 2"
});

db.Services.insert({
    name: "usluga3",
    price: NumberInt(30),
    createdDate: "2017-01-02",
    description: "opis uslugi 3"
});

db.Services.insert({
    name: "usluga4",
    price: NumberInt(60),
    createdDate: "2017-01-02",
    description: "opis uslugi 4"
});

print('* Inserted services into db');

print('All collections: ' + db.getCollectionNames());
print('* Script finished.');