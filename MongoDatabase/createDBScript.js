var db = connect('127.0.0.1:27017/BookingsServiceDB');
print('* Connected to database');

db.dropDatabase();

db.Services.drop();
db.createCollection("Services", {
    validator: {
        $and: [{
            serviceName: {
                $type: "string",
                $exists: true
            },
            time: {
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
    {serviceName: 1},
    {unique: true}
);
print('* Created collection Services. All collections: ' + db.getCollectionNames());

db.Users.drop();
db.createCollection("Users", {
    validator: {
        $and: [{
            UID: {
                $type: "string",
                $exists: true
            },
            password: {
                $type: "string",
                $exists: true
            },
            firstName: {
                $type: "string",
                $exists: true
            },
            lastName: {
                $type: "string",
                $exists: true
            },
            canBookService: {
                $type: "boolean",
                $exists: true
            },
            canSeeBookings: {
                $type: "boolean",
                $exists: true
            },
            canManageServices: {
                $type: "boolean",
                $exists: true
            }
        }]
    },
    validationAction: "error"
});

db.Users.createIndex(
    {UID: 1},
    {unique: true}
);
print('* Created collection Users. All collections: ' + db.getCollectionNames());

db.Bookings.drop();
db.createCollection("Bookings", {
    capped: true,
    size: 512000000,
    max: 100000,
    validator: {
        $and: [{
            serviceName: {
                $type: "string",
                $exists: true
            },
            UID: {
                $type: "int",
                $exists: true
            },
            requestedTime: {
                $type: "int",
                $exists: true
            },
            comment: {
                $type: "string",
                $exists: true
            }
        }]
    },
    validationAction: "error"
});

db.Bookings.createIndex(
    {UID: 1}
);
print('* Created collection Bookings. All collections: ' + db.getCollectionNames());

db.dropRole("BookingsServiceAppRole");
db.createRole({
    role: "BookingsServiceAppRole",
    privileges: [{
        resource: {
            db: "BookingsServiceDB",
            collection: "Users"
        },
        actions: ["find", "insert"]
    }, {
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

db.Users.insert({
    UID: "pbubel",
    password: "pbubel",
    firstName: true,
    lastName: true,
    canBookService: true,
    canSeeBookings: true,
    canManageServices: true
});
print('* Inserted user into db');

db.Services.insert({
    serviceName: "usluga1",
    time: NumberInt(15),
    description: "opis uslugi 1"
});

db.Services.insert({
    serviceName: "usluga2",
    time: NumberInt(30),
    description: "opis uslugi 2"
});

db.Services.insert({
    serviceName: "usluga3",
    time: NumberInt(60),
    description: "opis uslugi 3"
});

db.Services.insert({
    serviceName: "usluga4",
    time: NumberInt(24 * 60),
    description: "opis uslugi 4"
});

print('* Inserted services into db');

print('All collections: ' + db.getCollectionNames());
print('* Script finished.');