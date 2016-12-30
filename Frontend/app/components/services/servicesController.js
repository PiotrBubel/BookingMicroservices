'use strict';

myApp.controller("servicesController", function ($scope, $timeout, servicesFactory) {

    $scope.createNew = true;
    $scope.serviceData = {
        price: 1,
        minTime: 1,
        maxTime: 2
    };
    var clearData = angular.copy($scope.serviceData);

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
        servicesFactory.getList()
            .success(function (response) {
                $scope.services = response.list;
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd pobierania danych ', error.message);
            });
    };
    refreshList();

    $scope.changeSelected = function (item) {
        servicesFactory.getDetails(item)
            .success(function (response) {
                $scope.serviceData = response;
                $scope.wholeDay = ($scope.serviceData.maxTime === 24 * 60) && ($scope.serviceData.minTime === $scope.serviceData.maxTime);
                $scope.createNew = false;
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd pobierania danych ', error.message);
            });
    };

    $scope.setNew = function () {
        $scope.createNew = true;
        $scope.serviceData = angular.copy(clearData);
        delete($scope.serviceData.name);
        delete($scope.serviceData.description);
    };

    $scope.saveNewService = function () {
        console.log('$scope.serviceData', $scope.serviceData);
        if ($scope.wholeDay) {
            $scope.serviceData.minTime = $scope.serviceData.maxTime = 60 * 24;
        }
        servicesFactory.create($scope.serviceData)
            .success(function () {
                messageHandler.showSuccessMessage('Dodano pomyślnie');
                $scope.changeSelected($scope.serviceData.name);
                $scope.createNew = false;
                refreshList();
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd ', error.message);
            });
    };

    $scope.removeService = function () {
        servicesFactory.remove($scope.serviceData.name)
            .success(function () {
                messageHandler.showSuccessMessage('Usunięto pomyślnie');
                refreshList();
                $scope.setNew();
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd ', error.message);
            });
    };

    $scope.editService = function () {
        var newData = angular.copy($scope.serviceData);
        delete(newData.name);
        servicesFactory.edit($scope.serviceData.name, newData)
            .success(function () {
                messageHandler.showSuccessMessage('Edytowano pomyślnie');
                refreshList();
                $scope.setNew();
            })
            .error(function (error) {
                messageHandler.showErrorMessage('Błąd ', error.message);
            });
    };

    $scope.$watch('serviceData.maxTime + serviceData.minTime', function (newValue, oldValue) {
        if (!angular.equals(newValue, oldValue)) {
            $scope.wholeDay = ($scope.serviceData.maxTime === 24 * 60) && ($scope.serviceData.minTime === $scope.serviceData.maxTime);
        }
    });

    $scope.exists = function (givenObject) {
        return typeof givenObject !== 'undefined';
    }
});