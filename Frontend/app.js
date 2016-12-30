'use strict';

var myApp = angular.module('myApp', ['ngRoute', 'ngMessages'])

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
            }).when('/user', {
                controller: 'mainCtrl',
                templateUrl: '/app/components/user/userView.html'
            }).when('/users', {
                controller: 'usersController',
                templateUrl: '/app/components/users/usersView.html'
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
    // messageHandler.showMessage('dupka');
    // $http.get('http://localhost:8094/api/services').then(function (response) {
    //     $scope.services = response.data.list;
    //     console.log(response);
    // });
});


// myApp.factory("messageHandler", function ($timeout, $scope) {
//         this.showMessage = function(message) {
//             if(message) {
//                 $scope.message = message;
//             } else {
//                 $scope.message = 'Empty message';
//             }
//             $scope.showMessage = true;
//             $timeout(function() {
//                 $scope.showMessage = false;
//             }, 3000);
//         }
//     }
// );