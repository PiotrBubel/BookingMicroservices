'use strict';

myApp.controller("servicesController", function ($scope, $timeout, servicesFactory, $rootScope) {

    $scope.canManageServices = $rootScope.globalUser && $rootScope.globalUser.permissions && $rootScope.globalUser.permissions.canManageServices;

    $scope.createNew = true;
    $scope.services = [];
    $scope.serviceData = {
        description: '',
        price: 1,
        timePeriod: 60,
        maxPeriods: 2
    };
    $scope.helper = {
        wholeDay: true
    };

    var clearData = angular.copy($scope.serviceData);
    var rawData = angular.copy($scope.serviceData);

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
            .then(
                function (response) {
                    $scope.services = response.data.list;
                    if (!$scope.canManageServices) {
                        $scope.changeSelected($scope.services[0]);
                    }
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

    $scope.changeSelected = function (name) {
        servicesFactory.getDetails(name)
            .then(
                function (response) {
                    $scope.serviceData = response.data;
                    rawData = response.data;
                    $scope.helper.wholeDay = ($scope.serviceData.maxPeriods * $scope.serviceData.timePeriod === 60 * 24);
                    $scope.createNew = false;
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd pobierania danych ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.setNew = function () {
        $scope.createNew = true;
        $scope.serviceData = angular.copy(clearData);
        delete($scope.serviceData.name);
    };

    $scope.saveNewService = function () {
        servicesFactory.create($scope.serviceData)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Dodano pomyślnie');
                    $scope.changeSelected($scope.serviceData.name);
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

    $scope.removeService = function () {
        servicesFactory.remove($scope.serviceData.name)
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

    $scope.editService = function () {
        var newData = angular.copy($scope.serviceData);
        delete(newData.name);
        servicesFactory.edit($scope.serviceData.name, newData)
            .then(
                function () {
                    messageHandler.showSuccessMessage('Edytowano pomyślnie');
                    refreshList();
                    $scope.changeSelected($scope.serviceData.name);
                },
                function (error) {
                    if (error.data) {
                        messageHandler.showErrorMessage('Błąd ', error.data.message);
                    } else {
                        messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                    }
                });
    };

    $scope.changeWholeDay = function (wholeDay) {
        if (wholeDay) {
            $scope.serviceData.maxPeriods = 1;
            $scope.serviceData.timePeriod = 60 * 24;
        } else {
            $scope.serviceData.maxPeriods = rawData.maxPeriods;
            if (rawData.timePeriod >= 60 * 24) {
                $scope.serviceData.timePeriod = (60 * 24) - 1;
            } else {
                $scope.serviceData.timePeriod = rawData.timePeriod;
            }
        }
    };

    $scope.exists = function (givenObject) {
        return typeof givenObject !== 'undefined';
    }
});