; Transport p21-30-city-hubs-7hubs-4htrucks-6citysize-1ctrucks-20packages-2008

(define (problem transport-p21-30-city-hubs-7hubs-4htrucks-6citysize-1ctrucks-20packages-2008)
(:domain transport)
(:objects
city-0-0 - location
city-0-1 - location
city-0-2 - location
city-0-3 - location
city-0-4 - location
city-0-5 - location
city-1-0 - location
city-1-1 - location
city-1-2 - location
city-1-3 - location
city-1-4 - location
city-1-5 - location
city-2-0 - location
city-2-1 - location
city-2-2 - location
city-2-3 - location
city-2-4 - location
city-2-5 - location
city-3-0 - location
city-3-1 - location
city-3-2 - location
city-3-3 - location
city-3-4 - location
city-3-5 - location
city-4-0 - location
city-4-1 - location
city-4-2 - location
city-4-3 - location
city-4-4 - location
city-4-5 - location
city-5-0 - location
city-5-1 - location
city-5-2 - location
city-5-3 - location
city-5-4 - location
city-5-5 - location
city-6-0 - location
city-6-1 - location
city-6-2 - location
city-6-3 - location
city-6-4 - location
city-6-5 - location
hub-0 - location
hub-1 - location
hub-2 - location
hub-3 - location
hub-4 - location
hub-5 - location
hub-6 - location
ctruck-0-0 - vehicle
ctruck-1-0 - vehicle
ctruck-2-0 - vehicle
ctruck-3-0 - vehicle
ctruck-4-0 - vehicle
ctruck-5-0 - vehicle
ctruck-6-0 - vehicle
truck-0 - vehicle
truck-1 - vehicle
truck-2 - vehicle
truck-3 - vehicle
package-0 - package
package-1 - package
package-10 - package
package-11 - package
package-12 - package
package-13 - package
package-14 - package
package-15 - package
package-16 - package
package-17 - package
package-18 - package
package-19 - package
package-2 - package
package-3 - package
package-4 - package
package-5 - package
package-6 - package
package-7 - package
package-8 - package
package-9 - package
)
(:init

(road city-0-0 city-0-1)
(= (road-length city-0-0 city-0-1) 10)
(= (fuel-demand city-0-0 city-0-1) 12)

(road city-0-0 city-0-3)
(= (road-length city-0-0 city-0-3) 10)
(= (fuel-demand city-0-0 city-0-3) 12)

(road city-0-0 hub-0)
(= (road-length city-0-0 hub-0) 3)
(= (fuel-demand city-0-0 hub-0) 2)

(road city-0-1 city-0-0)
(= (road-length city-0-1 city-0-0) 10)
(= (fuel-demand city-0-1 city-0-0) 12)

(road city-0-1 city-0-2)
(= (road-length city-0-1 city-0-2) 10)
(= (fuel-demand city-0-1 city-0-2) 12)

(road city-0-2 city-0-1)
(= (road-length city-0-2 city-0-1) 10)
(= (fuel-demand city-0-2 city-0-1) 12)

(road city-0-2 city-0-3)
(= (road-length city-0-2 city-0-3) 10)
(= (fuel-demand city-0-2 city-0-3) 12)

(road city-0-3 city-0-0)
(= (road-length city-0-3 city-0-0) 10)
(= (fuel-demand city-0-3 city-0-0) 12)

(road city-0-3 city-0-2)
(= (road-length city-0-3 city-0-2) 10)
(= (fuel-demand city-0-3 city-0-2) 12)

(road city-0-3 city-0-4)
(= (road-length city-0-3 city-0-4) 10)
(= (fuel-demand city-0-3 city-0-4) 12)

(road city-0-4 city-0-3)
(= (road-length city-0-4 city-0-3) 10)
(= (fuel-demand city-0-4 city-0-3) 12)

(road city-0-4 city-0-5)
(= (road-length city-0-4 city-0-5) 10)
(= (fuel-demand city-0-4 city-0-5) 12)

(road city-0-5 city-0-4)
(= (road-length city-0-5 city-0-4) 10)
(= (fuel-demand city-0-5 city-0-4) 12)

(road city-1-0 city-1-1)
(= (road-length city-1-0 city-1-1) 10)
(= (fuel-demand city-1-0 city-1-1) 12)

(road city-1-0 city-1-2)
(= (road-length city-1-0 city-1-2) 10)
(= (fuel-demand city-1-0 city-1-2) 12)

(road city-1-0 city-1-5)
(= (road-length city-1-0 city-1-5) 10)
(= (fuel-demand city-1-0 city-1-5) 12)

(road city-1-1 city-1-0)
(= (road-length city-1-1 city-1-0) 10)
(= (fuel-demand city-1-1 city-1-0) 12)

(road city-1-1 city-1-2)
(= (road-length city-1-1 city-1-2) 10)
(= (fuel-demand city-1-1 city-1-2) 12)

(road city-1-2 city-1-0)
(= (road-length city-1-2 city-1-0) 10)
(= (fuel-demand city-1-2 city-1-0) 12)

(road city-1-2 city-1-1)
(= (road-length city-1-2 city-1-1) 10)
(= (fuel-demand city-1-2 city-1-1) 12)

(road city-1-2 city-1-3)
(= (road-length city-1-2 city-1-3) 10)
(= (fuel-demand city-1-2 city-1-3) 12)

(road city-1-3 city-1-2)
(= (road-length city-1-3 city-1-2) 10)
(= (fuel-demand city-1-3 city-1-2) 12)

(road city-1-3 city-1-4)
(= (road-length city-1-3 city-1-4) 10)
(= (fuel-demand city-1-3 city-1-4) 12)

(road city-1-4 city-1-3)
(= (road-length city-1-4 city-1-3) 10)
(= (fuel-demand city-1-4 city-1-3) 12)

(road city-1-4 hub-1)
(= (road-length city-1-4 hub-1) 3)
(= (fuel-demand city-1-4 hub-1) 2)

(road city-1-5 city-1-0)
(= (road-length city-1-5 city-1-0) 10)
(= (fuel-demand city-1-5 city-1-0) 12)

(road city-2-0 city-2-1)
(= (road-length city-2-0 city-2-1) 10)
(= (fuel-demand city-2-0 city-2-1) 12)

(road city-2-1 city-2-0)
(= (road-length city-2-1 city-2-0) 10)
(= (fuel-demand city-2-1 city-2-0) 12)

(road city-2-1 city-2-2)
(= (road-length city-2-1 city-2-2) 10)
(= (fuel-demand city-2-1 city-2-2) 12)

(road city-2-1 hub-2)
(= (road-length city-2-1 hub-2) 3)
(= (fuel-demand city-2-1 hub-2) 2)

(road city-2-2 city-2-1)
(= (road-length city-2-2 city-2-1) 10)
(= (fuel-demand city-2-2 city-2-1) 12)

(road city-2-2 city-2-3)
(= (road-length city-2-2 city-2-3) 10)
(= (fuel-demand city-2-2 city-2-3) 12)

(road city-2-2 city-2-5)
(= (road-length city-2-2 city-2-5) 10)
(= (fuel-demand city-2-2 city-2-5) 12)

(road city-2-3 city-2-2)
(= (road-length city-2-3 city-2-2) 10)
(= (fuel-demand city-2-3 city-2-2) 12)

(road city-2-3 city-2-4)
(= (road-length city-2-3 city-2-4) 10)
(= (fuel-demand city-2-3 city-2-4) 12)

(road city-2-4 city-2-3)
(= (road-length city-2-4 city-2-3) 10)
(= (fuel-demand city-2-4 city-2-3) 12)

(road city-2-4 city-2-5)
(= (road-length city-2-4 city-2-5) 10)
(= (fuel-demand city-2-4 city-2-5) 12)

(road city-2-5 city-2-2)
(= (road-length city-2-5 city-2-2) 10)
(= (fuel-demand city-2-5 city-2-2) 12)

(road city-2-5 city-2-4)
(= (road-length city-2-5 city-2-4) 10)
(= (fuel-demand city-2-5 city-2-4) 12)

(road city-3-0 city-3-1)
(= (road-length city-3-0 city-3-1) 10)
(= (fuel-demand city-3-0 city-3-1) 12)

(road city-3-0 city-3-3)
(= (road-length city-3-0 city-3-3) 10)
(= (fuel-demand city-3-0 city-3-3) 12)

(road city-3-0 city-3-5)
(= (road-length city-3-0 city-3-5) 10)
(= (fuel-demand city-3-0 city-3-5) 12)

(road city-3-1 city-3-0)
(= (road-length city-3-1 city-3-0) 10)
(= (fuel-demand city-3-1 city-3-0) 12)

(road city-3-1 city-3-2)
(= (road-length city-3-1 city-3-2) 10)
(= (fuel-demand city-3-1 city-3-2) 12)

(road city-3-2 city-3-1)
(= (road-length city-3-2 city-3-1) 10)
(= (fuel-demand city-3-2 city-3-1) 12)

(road city-3-2 city-3-3)
(= (road-length city-3-2 city-3-3) 10)
(= (fuel-demand city-3-2 city-3-3) 12)

(road city-3-3 city-3-0)
(= (road-length city-3-3 city-3-0) 10)
(= (fuel-demand city-3-3 city-3-0) 12)

(road city-3-3 city-3-2)
(= (road-length city-3-3 city-3-2) 10)
(= (fuel-demand city-3-3 city-3-2) 12)

(road city-3-3 city-3-4)
(= (road-length city-3-3 city-3-4) 10)
(= (fuel-demand city-3-3 city-3-4) 12)

(road city-3-4 city-3-3)
(= (road-length city-3-4 city-3-3) 10)
(= (fuel-demand city-3-4 city-3-3) 12)

(road city-3-4 hub-3)
(= (road-length city-3-4 hub-3) 3)
(= (fuel-demand city-3-4 hub-3) 2)

(road city-3-5 city-3-0)
(= (road-length city-3-5 city-3-0) 10)
(= (fuel-demand city-3-5 city-3-0) 12)

(road city-4-0 city-4-1)
(= (road-length city-4-0 city-4-1) 10)
(= (fuel-demand city-4-0 city-4-1) 12)

(road city-4-0 city-4-5)
(= (road-length city-4-0 city-4-5) 10)
(= (fuel-demand city-4-0 city-4-5) 12)

(road city-4-1 city-4-0)
(= (road-length city-4-1 city-4-0) 10)
(= (fuel-demand city-4-1 city-4-0) 12)

(road city-4-2 city-4-3)
(= (road-length city-4-2 city-4-3) 10)
(= (fuel-demand city-4-2 city-4-3) 12)

(road city-4-2 city-4-5)
(= (road-length city-4-2 city-4-5) 10)
(= (fuel-demand city-4-2 city-4-5) 12)

(road city-4-3 city-4-2)
(= (road-length city-4-3 city-4-2) 10)
(= (fuel-demand city-4-3 city-4-2) 12)

(road city-4-3 city-4-4)
(= (road-length city-4-3 city-4-4) 10)
(= (fuel-demand city-4-3 city-4-4) 12)

(road city-4-3 hub-4)
(= (road-length city-4-3 hub-4) 3)
(= (fuel-demand city-4-3 hub-4) 2)

(road city-4-4 city-4-3)
(= (road-length city-4-4 city-4-3) 10)
(= (fuel-demand city-4-4 city-4-3) 12)

(road city-4-4 city-4-5)
(= (road-length city-4-4 city-4-5) 10)
(= (fuel-demand city-4-4 city-4-5) 12)

(road city-4-5 city-4-0)
(= (road-length city-4-5 city-4-0) 10)
(= (fuel-demand city-4-5 city-4-0) 12)

(road city-4-5 city-4-2)
(= (road-length city-4-5 city-4-2) 10)
(= (fuel-demand city-4-5 city-4-2) 12)

(road city-4-5 city-4-4)
(= (road-length city-4-5 city-4-4) 10)
(= (fuel-demand city-4-5 city-4-4) 12)

(road city-5-0 city-5-1)
(= (road-length city-5-0 city-5-1) 10)
(= (fuel-demand city-5-0 city-5-1) 12)

(road city-5-0 city-5-5)
(= (road-length city-5-0 city-5-5) 10)
(= (fuel-demand city-5-0 city-5-5) 12)

(road city-5-1 city-5-0)
(= (road-length city-5-1 city-5-0) 10)
(= (fuel-demand city-5-1 city-5-0) 12)

(road city-5-1 city-5-2)
(= (road-length city-5-1 city-5-2) 10)
(= (fuel-demand city-5-1 city-5-2) 12)

(road city-5-2 city-5-1)
(= (road-length city-5-2 city-5-1) 10)
(= (fuel-demand city-5-2 city-5-1) 12)

(road city-5-2 city-5-3)
(= (road-length city-5-2 city-5-3) 10)
(= (fuel-demand city-5-2 city-5-3) 12)

(road city-5-3 city-5-2)
(= (road-length city-5-3 city-5-2) 10)
(= (fuel-demand city-5-3 city-5-2) 12)

(road city-5-3 city-5-5)
(= (road-length city-5-3 city-5-5) 10)
(= (fuel-demand city-5-3 city-5-5) 12)

(road city-5-3 hub-5)
(= (road-length city-5-3 hub-5) 3)
(= (fuel-demand city-5-3 hub-5) 2)

(road city-5-4 city-5-5)
(= (road-length city-5-4 city-5-5) 10)
(= (fuel-demand city-5-4 city-5-5) 12)

(road city-5-5 city-5-0)
(= (road-length city-5-5 city-5-0) 10)
(= (fuel-demand city-5-5 city-5-0) 12)

(road city-5-5 city-5-3)
(= (road-length city-5-5 city-5-3) 10)
(= (fuel-demand city-5-5 city-5-3) 12)

(road city-5-5 city-5-4)
(= (road-length city-5-5 city-5-4) 10)
(= (fuel-demand city-5-5 city-5-4) 12)

(road city-6-0 city-6-1)
(= (road-length city-6-0 city-6-1) 10)
(= (fuel-demand city-6-0 city-6-1) 12)

(road city-6-1 city-6-0)
(= (road-length city-6-1 city-6-0) 10)
(= (fuel-demand city-6-1 city-6-0) 12)

(road city-6-1 city-6-2)
(= (road-length city-6-1 city-6-2) 10)
(= (fuel-demand city-6-1 city-6-2) 12)

(road city-6-1 hub-6)
(= (road-length city-6-1 hub-6) 3)
(= (fuel-demand city-6-1 hub-6) 2)

(road city-6-2 city-6-1)
(= (road-length city-6-2 city-6-1) 10)
(= (fuel-demand city-6-2 city-6-1) 12)

(road city-6-2 city-6-3)
(= (road-length city-6-2 city-6-3) 10)
(= (fuel-demand city-6-2 city-6-3) 12)

(road city-6-3 city-6-2)
(= (road-length city-6-3 city-6-2) 10)
(= (fuel-demand city-6-3 city-6-2) 12)

(road city-6-3 city-6-4)
(= (road-length city-6-3 city-6-4) 10)
(= (fuel-demand city-6-3 city-6-4) 12)

(road city-6-3 city-6-5)
(= (road-length city-6-3 city-6-5) 10)
(= (fuel-demand city-6-3 city-6-5) 12)

(road city-6-4 city-6-3)
(= (road-length city-6-4 city-6-3) 10)
(= (fuel-demand city-6-4 city-6-3) 12)

(road city-6-4 city-6-5)
(= (road-length city-6-4 city-6-5) 10)
(= (fuel-demand city-6-4 city-6-5) 12)

(road city-6-5 city-6-3)
(= (road-length city-6-5 city-6-3) 10)
(= (fuel-demand city-6-5 city-6-3) 12)

(road city-6-5 city-6-4)
(= (road-length city-6-5 city-6-4) 10)
(= (fuel-demand city-6-5 city-6-4) 12)

(road hub-0 city-0-0)
(= (road-length hub-0 city-0-0) 3)
(= (fuel-demand hub-0 city-0-0) 2)

(road hub-0 hub-1)
(= (road-length hub-0 hub-1) 20)
(= (fuel-demand hub-0 hub-1) 15)

(road hub-0 hub-6)
(= (road-length hub-0 hub-6) 20)
(= (fuel-demand hub-0 hub-6) 15)

(road hub-1 city-1-4)
(= (road-length hub-1 city-1-4) 3)
(= (fuel-demand hub-1 city-1-4) 2)

(road hub-1 hub-0)
(= (road-length hub-1 hub-0) 20)
(= (fuel-demand hub-1 hub-0) 15)

(road hub-1 hub-2)
(= (road-length hub-1 hub-2) 20)
(= (fuel-demand hub-1 hub-2) 14)

(road hub-2 city-2-1)
(= (road-length hub-2 city-2-1) 3)
(= (fuel-demand hub-2 city-2-1) 2)

(road hub-2 hub-1)
(= (road-length hub-2 hub-1) 25)
(= (fuel-demand hub-2 hub-1) 16)

(road hub-2 hub-3)
(= (road-length hub-2 hub-3) 20)
(= (fuel-demand hub-2 hub-3) 15)

(road hub-3 city-3-4)
(= (road-length hub-3 city-3-4) 3)
(= (fuel-demand hub-3 city-3-4) 2)

(road hub-3 hub-2)
(= (road-length hub-3 hub-2) 20)
(= (fuel-demand hub-3 hub-2) 15)

(road hub-3 hub-4)
(= (road-length hub-3 hub-4) 20)
(= (fuel-demand hub-3 hub-4) 14)

(road hub-4 city-4-3)
(= (road-length hub-4 city-4-3) 3)
(= (fuel-demand hub-4 city-4-3) 2)

(road hub-4 hub-3)
(= (road-length hub-4 hub-3) 25)
(= (fuel-demand hub-4 hub-3) 16)

(road hub-4 hub-5)
(= (road-length hub-4 hub-5) 20)
(= (fuel-demand hub-4 hub-5) 15)

(road hub-5 city-5-3)
(= (road-length hub-5 city-5-3) 3)
(= (fuel-demand hub-5 city-5-3) 2)

(road hub-5 hub-4)
(= (road-length hub-5 hub-4) 20)
(= (fuel-demand hub-5 hub-4) 15)

(road hub-5 hub-6)
(= (road-length hub-5 hub-6) 20)
(= (fuel-demand hub-5 hub-6) 14)

(road hub-6 city-6-1)
(= (road-length hub-6 city-6-1) 3)
(= (fuel-demand hub-6 city-6-1) 2)

(road hub-6 hub-0)
(= (road-length hub-6 hub-0) 20)
(= (fuel-demand hub-6 hub-0) 15)

(road hub-6 hub-5)
(= (road-length hub-6 hub-5) 25)
(= (fuel-demand hub-6 hub-5) 16)
(at package-0 city-0-3)
(= (package-size package-0) 10)
(at package-1 city-1-4)
(= (package-size package-1) 20)
(at package-10 city-3-1)
(= (package-size package-10) 10)
(at package-11 city-4-2)
(= (package-size package-11) 20)
(at package-12 city-5-3)
(= (package-size package-12) 10)
(at package-13 city-6-4)
(= (package-size package-13) 20)
(at package-14 city-0-5)
(= (package-size package-14) 10)
(at package-15 city-1-0)
(= (package-size package-15) 20)
(at package-16 city-2-1)
(= (package-size package-16) 10)
(at package-17 city-3-2)
(= (package-size package-17) 20)
(at package-18 city-4-3)
(= (package-size package-18) 10)
(at package-19 city-5-4)
(= (package-size package-19) 20)
(at package-2 city-2-5)
(= (package-size package-2) 10)
(at package-3 city-3-0)
(= (package-size package-3) 20)
(at package-4 city-4-1)
(= (package-size package-4) 10)
(at package-5 city-5-2)
(= (package-size package-5) 20)
(at package-6 city-6-3)
(= (package-size package-6) 10)
(at package-7 city-0-4)
(= (package-size package-7) 20)
(at package-8 city-1-5)
(= (package-size package-8) 10)
(at package-9 city-2-0)
(= (package-size package-9) 20)
(has-petrol-station hub-0)
(has-petrol-station hub-1)
(has-petrol-station hub-2)
(has-petrol-station hub-3)
(has-petrol-station hub-4)
(has-petrol-station hub-5)
(has-petrol-station hub-6)
(at ctruck-0-0 hub-0)
(ready-loading ctruck-0-0)
(= (capacity ctruck-0-0) 30)
(= (fuel-left ctruck-0-0) 0)
(= (fuel-max ctruck-0-0) 124)
(at ctruck-1-0 hub-1)
(ready-loading ctruck-1-0)
(= (capacity ctruck-1-0) 30)
(= (fuel-left ctruck-1-0) 0)
(= (fuel-max ctruck-1-0) 124)
(at ctruck-2-0 hub-2)
(ready-loading ctruck-2-0)
(= (capacity ctruck-2-0) 30)
(= (fuel-left ctruck-2-0) 0)
(= (fuel-max ctruck-2-0) 124)
(at ctruck-3-0 hub-3)
(ready-loading ctruck-3-0)
(= (capacity ctruck-3-0) 30)
(= (fuel-left ctruck-3-0) 0)
(= (fuel-max ctruck-3-0) 124)
(at ctruck-4-0 hub-4)
(ready-loading ctruck-4-0)
(= (capacity ctruck-4-0) 30)
(= (fuel-left ctruck-4-0) 0)
(= (fuel-max ctruck-4-0) 124)
(at ctruck-5-0 hub-5)
(ready-loading ctruck-5-0)
(= (capacity ctruck-5-0) 30)
(= (fuel-left ctruck-5-0) 0)
(= (fuel-max ctruck-5-0) 124)
(at ctruck-6-0 hub-6)
(ready-loading ctruck-6-0)
(= (capacity ctruck-6-0) 30)
(= (fuel-left ctruck-6-0) 0)
(= (fuel-max ctruck-6-0) 124)
(at truck-0 hub-0)
(ready-loading truck-0)
(= (capacity truck-0) 100)
(= (fuel-left truck-0) 0)
(= (fuel-max truck-0) 105)
(at truck-1 hub-1)
(ready-loading truck-1)
(= (capacity truck-1) 100)
(= (fuel-left truck-1) 0)
(= (fuel-max truck-1) 105)
(at truck-2 hub-2)
(ready-loading truck-2)
(= (capacity truck-2) 100)
(= (fuel-left truck-2) 0)
(= (fuel-max truck-2) 105)
(at truck-3 hub-3)
(ready-loading truck-3)
(= (capacity truck-3) 100)
(= (fuel-left truck-3) 0)
(= (fuel-max truck-3) 105)
)
(:goal (and
(at package-0 city-3-3)
(at package-1 city-4-2)
(at package-10 city-6-5)
(at package-11 city-0-4)
(at package-12 city-1-3)
(at package-13 city-2-2)
(at package-14 city-3-1)
(at package-15 city-4-0)
(at package-16 city-5-5)
(at package-17 city-6-4)
(at package-18 city-0-3)
(at package-19 city-1-2)
(at package-2 city-5-1)
(at package-3 city-6-0)
(at package-4 city-0-5)
(at package-5 city-1-4)
(at package-6 city-2-3)
(at package-7 city-3-2)
(at package-8 city-4-1)
(at package-9 city-5-0)
(at ctruck-0-0 hub-0)
(at ctruck-1-0 hub-1)
(at ctruck-2-0 hub-2)
(at ctruck-3-0 hub-3)
(at ctruck-4-0 hub-4)
(at ctruck-5-0 hub-5)
(at ctruck-6-0 hub-6)
(at truck-0 hub-0)
(at truck-1 hub-1)
(at truck-2 hub-2)
(at truck-3 hub-3)
))
(:metric minimize (total-time))
)
