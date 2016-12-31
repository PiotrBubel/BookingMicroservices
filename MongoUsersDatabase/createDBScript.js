var db = connect('127.0.0.1:27017/BookingsServiceUsersDB');
print('* Connected to database');

db.dropDatabase();

db.Users.drop();
db.createCollection("Users", {
    validator: {
        $and: [{
            login: {
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
            email: {
                $type: "string",
                $exists: true
            },
            "permissions.canManageServices": {
                $type: "boolean",
                $exists: true
            },
            "permissions.canManageUsers": {
                $type: "boolean",
                $exists: true
            },
            "permissions.canManageBookings": {
                $type: "boolean",
                $exists: true
            }
        }]
    },
    validationAction: "error"
});

db.Users.createIndex(
    {login: 1},
    {unique: true}
);
print('* Created collection Users. All collections: ' + db.getCollectionNames());

db.dropRole("BookingsServiceUsersAppRole");
db.createRole({
    role: "BookingsServiceUsersAppRole",
    privileges: [{
        resource: {
            db: "BookingsServiceUsersDB",
            collection: "Users"
        },
        actions: ["find", "insert"]
    }],
    roles: []
});
print('* Created role BookingsServiceUsersAppRole');

db.dropUser("BookingsServiceUsersApp");
db.createUser({
    user: "BookingsServiceUsersApp",
    pwd: "BookingsServicep@Ssw0rd",
    roles: [{
        role: "BookingsServiceUsersAppRole",
        db: "BookingsServiceUsersDB"
    }]
});
print('* Created user BookingsServiceUsersApp, pwd: BookingsServicep@Ssw0rd');

db.Users.insert({
    login: "pbubel",
    password: "pbubel",
    firstName: true,
    lastName: true,
    email: "piotr.bubel@wp.pl",
    permissions: {
        canManageServices: true,
        canManageUsers: true,
        canManageBookings: true
    }
});
print('* Inserted user into db');

print('All collections: ' + db.getCollectionNames());
print('* Script finished.');