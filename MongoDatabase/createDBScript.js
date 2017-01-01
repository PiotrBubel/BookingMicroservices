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
            timePeriod: {
                $type: "int",
                $exists: true
            },
            maxPeriods: {
                $type: "int",
                $exists: true
            },
            description: {
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
            startDate: {
                $type: "string",
                $exists: true
            },
            periods: {
                $type: "int",
                $exists: true
            },
            description: {
                $type: "string",
                $exists: true
            }
        }]
    },
    validationAction: "error"
});

db.Bookings.createIndex(
    {userLogin: 1}
);
print('* Created collection Bookings. All collections: ' + db.getCollectionNames());

db.dropRole("BookingsServiceAppRole");
db.createRole({
    role: "BookingsServiceAppRole",
    privileges: [{
        resource: {
            db: "BookingsServiceDB",
            collection: "Bookings"
        },
        actions: ["find", "insert"]
    }, {
        resource: {
            db: "BookingsServiceDB",
            collection: "Services"
        },
        actions: ["find", "insert"]
    }, {
        resource: {
            db: "BookingsServiceDB",
            collection: "Settings"
        },
        actions: ["find", "insert"]
    }],
    roles: []
});
print('* Created role BookingsServiceAppRole');

db.dropUser("BookingsServiceApp");
db.createUser({
    user: "BookingsServiceApp",
    pwd: "BookingsServicep@Ssw0rd",
    roles: [{
        role: "BookingsServiceAppRole",
        db: "BookingsServiceDB"
    }]
});
print('* Created user BookingsServiceApp, pwd: BookingsServicep@Ssw0rd');

db.Services.insert({
    name: "usluga1",
    timePeriod: NumberInt(60),
    maxPeriods: NumberInt(2),
    price: NumberInt(15),
    description: "opis uslugi 1"
});

db.Services.insert({
    name: "usluga2",
    timePeriod: NumberInt(30),
    maxPeriods: NumberInt(4),
    price: NumberInt(15),
    description: "opis uslugi 2"
});

db.Services.insert({
    name: "usluga3",
    timePeriod: NumberInt(15),
    maxPeriods: NumberInt(6),
    price: NumberInt(30),
    description: "opis uslugi 3"
});

db.Services.insert({
    name: "usluga4",
    timePeriod: NumberInt(15),
    maxPeriods: NumberInt(4),
    price: NumberInt(60),
    description: "opis uslugi 4"
});

db.Bookings.insert({
    serviceName: "usluga4",
    userLogin: "pbubel",
    startDate: "2017-01-02;10:00",
    periods: NumberInt(3),
    description: "opis uslugi 4"
});

db.Bookings.insert({
    serviceName: "usluga4",
    userLogin: "pbubel",
    startDate: "2017-01-02;13:00",
    periods: NumberInt(3),
    description: "opis uslugi 4"
});

print('* Inserted services into db');

print('All collections: ' + db.getCollectionNames());
print('* Script finished.');