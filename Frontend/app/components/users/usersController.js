'use strict';

myApp.controller("usersController", function ($scope, $timeout, usersFactory) {

    $scope.createNew = true;
    $scope.userData = {
        description: '',
        price: 1,
        minTime: 1,
        maxTime: 2
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

    $scope.changeSelected = function (name) {
        usersFactory.getDetails(name)
            .success(function (response) {
                $scope.userData = response;
                $scope.wholeDay = ($scope.userData.maxTime === 24 * 60) && ($scope.userData.minTime === $scope.userData.maxTime);
                $scope.createNew = false;
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd pobierania danych ', error.message);
            });
    };

    $scope.setNew = function () {
        $scope.createNew = true;
        $scope.userData = angular.copy(clearData);
        delete($scope.userData.name);
    };

    $scope.saveNewUser = function () {
        console.log('$scope.userData', $scope.userData);
        if ($scope.wholeDay) {
            $scope.userData.minTime = $scope.userData.maxTime = 60 * 24;
        }
        usersFactory.create($scope.userData)
            .success(function () {
                messageHandler.showSuccessMessage('Dodano pomyślnie');
                $scope.changeSelected($scope.userData.name);
                $scope.createNew = false;
                refreshList();
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd ', error.message);
            });
    };

    $scope.removeUser = function () {
        usersFactory.remove($scope.userData.name)
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
        delete(newData.name);
        usersFactory.edit($scope.userData.name, newData)
            .success(function () {
                messageHandler.showSuccessMessage('Edytowano pomyślnie');
                refreshList();
                $scope.changeSelected($scope.userData.name);
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd ', error.message);
            });
    };

    $scope.exists = function (givenObject) {
        return typeof givenObject !== 'undefined';
    }
});