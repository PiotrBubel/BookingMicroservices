'use strict';

var myApp = angular.module('myApp', ['ngRoute'])

    .config(['$routeProvider', '$locationProvider',
        function ($routeProvider, $locationProvider) {
            $routeProvider.when('/login', {
                controller: 'mainCtrl',
                templateUrl: '/app/components/user/loginView.html'
            }).when('/services', {
                controller: 'servicesController',
                templateUrl: '/app/components/services/servicesView.html'
            }).when('/bookings', {
                controller: 'mainCtrl',
                templateUrl: '/app/components/bookings/bookingsView.html'
            }).when('/statistics', {
                controller: 'mainCtrl',
                templateUrl: '/app/components/statistics/statisticsView.html'
            }).when('/settings', {
                controller: 'mainCtrl',
                templateUrl: '/app/components/user/userView.html'
            }).when('/main', {
                controller: 'mainCtrl',
                templateUrl: '/app/components/main/mainView.html'
            }).otherwise({
                redirectTo: '/'
            });
            $locationProvider.html5Mode({enabled: true, requireBase: false});
        }]);

myApp.controller("mainCtrl", function ($http, $scope) {
    $scope.UID = 'wesole testy';
    $scope.userLoggedIn = true;
    console.log('main controller');
    // $http.get('http://localhost:8094/api/services').then(function (response) {
    //     $scope.services = response.data.list;
    //     console.log(response);
    // });
});