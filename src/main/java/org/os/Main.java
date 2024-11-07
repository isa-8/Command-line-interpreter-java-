package org.os;
import java.io.*;
import java.nio.file.*;
import java.util.Scanner;
import java.util.Arrays;
import org.os.Commands;


public class Main {
    private static Path currentDirectory = Paths.get(System.getProperty("user.dir"));

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Commands commands = new Commands();

        System.out.println("Welcome to our Command line interpreter using Java. Type 'help' for available commands.");

        while (true) {
            System.out.print(currentDirectory + " > ");
            String input = scanner.nextLine().trim();
            String[] command = input.split(" ");

            if (command.length == 0) {
                continue; // Skip if input is empty
            }
            if(input.contains(">")) {
                commands.redirect(input);
            }

            try {


                switch (command[0]) {
                    case "exit":
                        System.out.println("Exiting CLI...");
                        scanner.close();
                        return;
                    case "help":
                        commands.help();
                        break;
                    case "pwd":
                        commands.pwd(currentDirectory);
                        break;
                    case "cd":
                        currentDirectory = commands
                                .cd(command, currentDirectory);
                        break;
                    case "ls":
                        commands.ls(currentDirectory);
                        break;
                    case "ls-a":
                        commands.lsa(currentDirectory);
                        break;
                    case "ls-r":
                        commands.lsr(currentDirectory);
                        break;
                    case "mkdir":
                        commands.mkdir(command, currentDirectory);
                        break;
                    case "rmdir":
                        commands.rmdir(command, currentDirectory);
                        break;
                    case "touch":
                        if (command.length > 1) {
                            commands.touch(command[1], currentDirectory); // Pass only the second element
                        } else {
                            System.out.println("Usage: touch <filename>");
                        }
                        break;
                    case "mv":
                        commands.mv(command, currentDirectory);
                        break;
                    case "rm":
                        if (command.length > 1) {
                            commands.rm(command[1], currentDirectory); // Pass only the second element
                        } else {
                            System.out.println("Usage: rm <filename>");
                        }
                        break;
                    case "cat":
                        commands.cat(command, currentDirectory);
                        break;
                    case "grep":
                        if (command.length >= 3) {
                            commands.grep(command, currentDirectory); // Pass the full command array
                        } else {
                            System.out.println("Usage: grep <pattern> <filename>");
                        }
                        break;
                    default:
                        System.out.println("Unknown command: '" + command[0] + "' , please try again or use 'help' to browse available commands.");
                }
            } catch (Exception e) {
                // Handle generic exceptions, including IOException if thrown from any command methods
                System.err.println("An error occurred: " + e.getMessage());
            }
        }
    }
}