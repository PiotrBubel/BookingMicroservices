'use strict';

myApp.controller("servicesController", function ($scope, $timeout, servicesFactory, $rootScope) {

    $scope.canManageServices = $rootScope.globalUser && $rootScope.globalUser.permissions && $rootScope.globalUser.permissions.canManageServices;

    $scope.createNew = true;
    $scope.serviceData = {
        description: '',
        price: 1,
        timePeriod: 60,
        maxPeriods: 2
    };
    $scope.helper = {
        wholeDay: false
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
            .success(function (response) {
                $scope.services = response.list;
                if (!$scope.canManageServices) {
                    $scope.changeSelected($scope.services[0]);
                }
            })
            .error(function (error) {
                if (error) {
                    messageHandler.showErrorMessage('Błąd pobierania danych ', error.message);
                } else {
                    messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                }
            });
    };
    refreshList();

    $scope.changeSelected = function (name) {
        servicesFactory.getDetails(name)
            .success(function (response) {
                $scope.serviceData = response;
                rawData = response;
                $scope.helper.wholeDay = ($scope.serviceData.maxPeriods * $scope.serviceData.timePeriod === 60 * 24);
                $scope.createNew = false;
            })
            .error(function (error) {
                if (error) {
                    messageHandler.showErrorMessage('Błąd pobierania danych ', error.message);
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
            .success(function () {
                messageHandler.showSuccessMessage('Dodano pomyślnie');
                $scope.changeSelected($scope.serviceData.name);
                $scope.createNew = false;
                refreshList();
            })
            .error(function (error) {
                if (error) {
                    messageHandler.showErrorMessage('Błąd ', error.message);
                } else {
                    messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                }
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
                if (error) {
                    messageHandler.showErrorMessage('Błąd ', error.message);
                } else {
                    messageHandler.showErrorMessage('Błąd ', "Brak połączenia z API");
                }
            });
    };

    $scope.editService = function () {
        var newData = angular.copy($scope.serviceData);
        delete(newData.name);
        servicesFactory.edit($scope.serviceData.name, newData)
            .success(function () {
                messageHandler.showSuccessMessage('Edytowano pomyślnie');
                refreshList();
                $scope.changeSelected($scope.serviceData.name);
            })
            .error(function (error) {
                if (error) {
                    messageHandler.showErrorMessage('Błąd ', error.message);
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