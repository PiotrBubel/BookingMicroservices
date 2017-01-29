var db = connect('127.0.0.1:27017/BookingsServiceDB');
print('* Connected to database');

db.dropDatabase();

db.Bookings.drop();
db.createCollection("Bookings", {
    validator: {
        $and: [{
            serviceName: {
                $type: "string",
                $exists: true
            },
            userLogin: {
                $type: "string",
                $exists: true
            },
            date: {
                $type: "string",
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

db.Bookings.createIndex(
    {
        userLogin: 1,
        serviceName: 1,
        date: 1
    },
    {unique: true}
);
print('* Created collection Bookings. All collections: ' + db.getCollectionNames());

db.dropRole("BookingsDatabaseProxyRole");
db.createRole({
    role: "BookingsDatabaseProxyRole",
    privileges: [{
        resource: {
            db: "BookingsServiceDB",
            collection: "Bookings"
        },
        actions: [ "find", "update", "insert", "remove" ]
    }],
    roles: []
});
print('* Created role BookingsServiceAppUser');

db.dropUser("BookingsDatabaseProxyUser");
db.createUser({
    user: "BookingsDatabaseProxyUser",
    pwd: "BookingsServicep@Ssw0rd",
    roles: [{
        role: "BookingsDatabaseProxyRole",
        db: "BookingsServiceDB"
    }]
});
print('* Created user BookingsDatabaseProxyUser');

print('All collections: ' + db.getCollectionNames());
print('* Script finished.');