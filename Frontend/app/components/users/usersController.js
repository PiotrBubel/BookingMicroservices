'use strict';

myApp.controller("usersController", function ($scope, $timeout, usersFactory, $rootScope) {

    $scope.canManageUsers = $rootScope.globalUser && $rootScope.globalUser.permissions && $rootScope.globalUser.permissions.canManageUsers;

    $scope.createNew = true;
    $scope.userData = {
        permissions: {
            canManageServices: false,
            canManageUsers: false,
            canManageBookings: false
        }
    };
    $scope.users = [];

    var clearData = angular.copy($scope.userData);

    var messageHandler = {};
    messageHandler.showErrorMessage = function (message, description) {
        if (message) {
            $scope.errorMessage = message;
        } else {
            $scope.errorMessage = 'Błąd';
        }
        if (description) {
            $scope.errorDescription = description;
        } else {
            $scope.errorDescription = '';
        }
        $scope.showErrorMessage = true;
        $(".alert-danger").hide().show('medium');
        $timeout(function () {
            $scope.showErrorMessage = false;
        }, 3000);
    };

    messageHandler.showSuccessMessage = function (message, description) {
        if (message) {
            $scope.successMessage = message;
        } else {
            $scope.successMessage = 'Sukces';
        }
        if (description) {
            $scope.successDescription = description;
        } else {
            $scope.successDescription = '';
        }
        $scope.showSuccessMessage = true;
        $(".alert-success").hide().show('medium');
        $timeout(function () {
            $scope.showSuccessMessage = false;
        }, 3000);
    };

    var refreshList = function () {
        usersFactory.getList()
            .then(
                function (response) {
                    $scope.users = response.data.list;
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania danych ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };
    refreshList();

    $scope.changeSelected = function (login) {
        usersFactory.getDetails(login)
            .then(
                function (response) {
                    $scope.userData = response.data;
                    $scope.createNew = false;
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania danych', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.setNew = function () {
        $scope.createNew = true;
        $scope.userData = angular.copy(clearData);
        delete($scope.userData.login);
        delete($scope.userData.password);
        delete($scope.userData.firstName);
        delete($scope.userData.lastName);
        delete($scope.userData.email);
    };

    $scope.saveNewUser = function () {
        usersFactory.create($scope.userData)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Dodano pomyślnie');
                    $scope.changeSelected($scope.userData.login);
                    $scope.createNew = false;
                    refreshList();
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.removeUser = function () {
        usersFactory.remove($scope.userData.login)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Usunięto pomyślnie');
                    refreshList();
                    $scope.setNew();
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.editUser = function () {
        var newData = angular.copy($scope.userData);
        delete(newData.login);
        usersFactory.edit($scope.userData.login, newData)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Edytowano pomyślnie');
                    refreshList();
                    $scope.changeSelected($scope.userData.login);
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.exists = function (givenObject) {
        return typeof givenObject !== 'undefined';
    }
});