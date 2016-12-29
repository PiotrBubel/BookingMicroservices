'use strict';

myApp.controller("servicesController", function ($http, $scope) {

    $scope.createNew = true;
    $scope.newService = {
        name: '',
        description: '',
        price: 0,
        suggestedTime: 0
    };
    var clearData = angular.copy($scope.newService);

    var refreshList = function () {
        $http.get('http://localhost:8094/api/services')
            .success(function (response) {
                console.log(response);
                $scope.services = response.list;
            })
            .error(function (error, status) {
                console.log('error', error);
                console.log('status', status);
            });
    };
    refreshList();

    $scope.changeSelected = function (item) {
        $http.get('http://localhost:8094/api/services/' + item)
            .success(function (response) {
                console.log(response);
                $scope.selected = response;
                $scope.createNew = false;
            })
            .error(function (error, status) {
                console.log('error', error);
                console.log('status', status);
            });
    };

    $scope.setNew = function () {
        $scope.createNew = true;
        $scope.newService = angular.copy(clearData);
    };

    $scope.saveNewService = function () {
        console.log('$scope.newService', $scope.newService);
        $http.post('http://localhost:8094/api/services', {service: $scope.newService})
            .success(function (response) {
                console.log(response);
                $scope.selected = $scope.newService;
                $scope.createNew = false;
                refreshList();
            })
            .error(function (error, status) {
                console.log('error', error);
                console.log('status', status);
            });
    };

    $scope.removeService = function () {
        $http.delete('http://localhost:8094/api/services/' + $scope.selected.name).then(function (response) {
            refreshList();
            $scope.setNew();
        });
    };

    $scope.exists = function (givenObject) {
        return typeof givenObject !== 'undefined';
    }
});