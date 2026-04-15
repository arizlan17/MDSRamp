package org.example;

import org.example.pojo.CustomerPojo;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        ArrayList<CustomerPojo> customers = new ArrayList<>();
        final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
        final String DB_USER = "postgres";
        final String DB_PASSWORD = "1234";


        try (Connection dbConnection = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD)) {

            Statement statement = dbConnection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS customers (id SERIAL PRIMARY KEY, name VARCHAR(255), email VARCHAR(255), age INTEGER, phone_number VARCHAR(20), date_of_birth DATE, city VARCHAR(255), is_employed BOOLEAN, salary DECIMAL , state VARCHAR(255), zip_code VARCHAR(20), country VARCHAR(255))");

            ResultSet resultSet = statement.executeQuery("SELECT * FROM customers");

            while (resultSet.next()) {
                customers.add(new CustomerPojo() {
                    {
                    setName(resultSet.getString("name"));
                    setEmail(resultSet.getString("email"));
                    setAge(resultSet.getInt("age"));
                    setPhoneNumber(resultSet.getString("phone_number"));
                    setDateOfBirth(resultSet.getDate("date_of_birth"));
                    setCity(resultSet.getString("city"));
                    setEmployed(resultSet.getBoolean("is_employed"));
                    setSalary(resultSet.getDouble("salary"));
                    setState(resultSet.getString("state"));
                    setZipCode(resultSet.getString("zip_code"));
                    setCountry(resultSet.getString("country"));

                }
                });

            }


            System.out.println("Customers Employed Customers:");
            List<CustomerPojo> employedCustomer = customers.stream().filter(CustomerPojo::isEmployed).toList();
            System.out.println(employedCustomer);


            System.out.println("\n\nCustomers Grouped by City:");
            HashMap<String, List<CustomerPojo>> customersByCity = new HashMap<>();
            customers.forEach(customer -> {
                String city = customer.getCity();
                if (!customersByCity.containsKey(city)) {
                    customersByCity.put(city, new ArrayList<>());
                }
                customersByCity.get(city).add(customer);
            });


            System.out.println(customersByCity);


            System.out.println("Print All Customers:");

            customers.forEach(
                    customer -> System.out.println(customer.toString())
            );


            System.out.println("\n\nCalculate Date for Each Customer:");

calculateDate(customers);



        } catch (SQLException e) {
            e.printStackTrace();
        }
        }


        private static void calculateDate(List<CustomerPojo> customers){

        customers.forEach(customer -> {
            customer.setCalculatedDate(LocalDateTime.now().minusDays(6).minusMonths(2));
        });

        customers.forEach(System.out::println);

        }
    }
