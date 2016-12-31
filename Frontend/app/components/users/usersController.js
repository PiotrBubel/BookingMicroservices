'use strict';

myApp.controller("usersController", function ($scope, $timeout, usersFactory) {

    $scope.createNew = true;
    $scope.userData = {
        permissions: {
            canManageServices: false,
            canManageUsers: false,
            canManageBookings: false
        }
    };

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
            .success(function (response) {
                $scope.users = response.list;
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd pobierania danych ', error.message);
            });
    };
    refreshList();

    $scope.changeSelected = function (login) {
        usersFactory.getDetails(login)
            .success(function (response) {
                $scope.userData = response;
                $scope.createNew = false;
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd pobierania danych ', error.message);
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
        console.log('$scope.userData', $scope.userData);
        usersFactory.create($scope.userData)
            .success(function () {
                messageHandler.showSuccessMessage('Dodano pomyślnie');
                $scope.changeSelected($scope.userData.login);
                $scope.createNew = false;
                refreshList();
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd ', error.message);
            });
    };

    $scope.removeUser = function () {
        usersFactory.remove($scope.userData.login)
            .success(function () {
                messageHandler.showSuccessMessage('Usunięto pomyślnie');
                refreshList();
                $scope.setNew();
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd ', error.message);
            });
    };

    $scope.editUser = function () {
        var newData = angular.copy($scope.userData);
        delete(newData.login);
        usersFactory.edit($scope.userData.login, newData)
            .success(function () {
                messageHandler.showSuccessMessage('Edytowano pomyślnie');
                refreshList();
                $scope.changeSelected($scope.userData.login);
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd ', error.message);
            });
    };

    $scope.exists = function (givenObject) {
        return typeof givenObject !== 'undefined';
    }
});