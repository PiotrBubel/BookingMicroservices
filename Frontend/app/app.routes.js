// /**
//  * Created by pbubel on 28.12.16.
//  */
// var router = angular.module('router', ['$routeProvider', '$locationProvider'])
//     .config(['$routeProvider', '$locationProvider',
//         function ($routeProvider, $locationProvider) {
//         console.log('dupka')
//             $routeProvider.when('/login', {
//                 controller: 'mainCtrl',
//                 templateUrl: '/app/components/user/loginView.html'
//             }).when('/services', {
//                 controller: 'mainCtrl',
//                 templateUrl: '/app/components/services/servicesView.html'
//             }).when('/bookings', {
//                 controller: 'mainCtrl',
//                 templateUrl: '/app/components/bookings/bookingsView.html'
//             }).when('/settings', {
//                 controller: 'mainCtrl',
//                 templateUrl: '/app/components/user/userView.html'
//             }).when('/main', {
//                 controller: 'mainCtrl',
//                 templateUrl: '/app/components/main/mainView.html'
//             }).otherwise({
//                 redirectTo: '/main'
//             });
//             $locationProvider.html5Mode({enabled: true, requireBase: false});
//         }]);