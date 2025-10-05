package com.example.courseprifs.consoleCourseWork;

import com.example.courseprifs.model.Driver;
import com.example.courseprifs.model.User;
import com.example.courseprifs.model.VehicleType;

import java.time.LocalDate;
import java.util.Scanner;

public class MenuControl {
    public static void generateUserMenu(Scanner scanner, Wolt wolt) {
        var cmd = 0;
        while (cmd != 6) {
            System.out.println("""
                    Choose and option:
                    1 - create
                    2 - view all users
                    3 - view user
                    4 - update user
                    5 - delete user
                    6 - return to main menu
                    """);
            cmd = scanner.nextInt();
            scanner.nextLine();
            var input = "";
            switch (cmd) {
                case 1:
                    System.out.println("Enter User data (User class):username;password;name;surname;phoneNum;address; licence; bdate;vehicle");
                    input = scanner.nextLine();
                    String[] info = input.split(";");
                    User user = new User(info[0], info[1], info[2], info[3], info[4]);
                    wolt.getAllSystemUsers().add(user);
                    //Driver driver = new Driver(info[0], info[1], info[2], info[3], info[4], info[5], info[6], LocalDate.parse(info[7]), VehicleType.valueOf(info[8]));
                    Utils.writeUserToFile(user);
                    break;
                case 2:
                    for (User u : wolt.getAllSystemUsers()) {
                        System.out.println(u);
                    }
                    break;
                case 3:
                    System.out.println("enter login:");
                    input = scanner.nextLine();
                    for (User u : wolt.getAllSystemUsers()) {
                        if (u.getLogin().equals(input)) {
                            System.out.println(u);
                        }
                    }
                    //User userSpecific = wolt.getAllSystemUsers().stream().filter(u->u.getLogin().equals(inputForSearch)).findAny().orElse(null);
                    break;
                case 4:
                    System.out.println("enter login:");
                    input = scanner.nextLine();
                    for (User u : wolt.getAllSystemUsers()) {
                        if (u.getLogin().equals(input));
                        System.out.println("Update data: name;surname");
                        String[] update = scanner.nextLine().split(";");
                        u.setSurname(update[1]);
                        u.setName(update[0]);
                        System.out.println(u);
                    }
                    break;
                case 5:
                    System.out.println("enter login:");
                    input = scanner.nextLine();
                    for (User u : wolt.getAllSystemUsers()) {
                        if (u.getLogin().equals(input)) {
                            wolt.getAllSystemUsers().remove(u);
                        }
                    }
                    break;
                default:
                    System.out.println();
            }
        }
    }
}
