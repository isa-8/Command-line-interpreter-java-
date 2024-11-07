package org.os;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Stream;

public class Commands {

    public void help() {
        System.out.println("Available Commands:");
        System.out.println("pwd                  : Display current directory");
        System.out.println("cd <dir>             : Change to the specified directory");
        System.out.println("ls                   : List files in the current directory");
        System.out.println("ls -a                : List all files, including hidden files, in the current directory");
        System.out.println("ls -r                : Recursively list all files and directories");
        System.out.println("mkdir <dir>          : Create a new directory with the specified name");
        System.out.println("rmdir <dir>          : Remove an empty directory with the specified name");
        System.out.println("touch <file>         : Create a new file with the specified name");
        System.out.println("mv <src> <dest>      : Move or rename a file or directory from <src> to <dest>");
        System.out.println("rm <file>            : Remove a file with the specified name");
        System.out.println("cat <file>           : Display the content of the specified file");
        System.out.println("echo <text> > <file> : Redirects the output of 'echo' to a file (overwrites)");
        System.out.println("echo <text> >> <file>: Redirects the output of 'echo' to a file (appends)");
        System.out.println("exit                 : Terminate the command line interpreter");
        System.out.println("help                 : Display this help message");
    }





    public void ls(Path currentDirectory) {
        // Basic `ls` - lists only visible files and directories in the current directory
        try (Stream<Path> paths = Files.list(currentDirectory)) {
            paths.filter(path -> !path.getFileName().toString().startsWith("."))
                    .forEach(path -> {
                        if (Files.isDirectory(path)) {
                            System.out.println(path.getFileName() + "/");
                        } else {
                            System.out.println(path.getFileName());
                        }
                    });
        } catch (IOException e) {
            System.out.println("ls: Error reading directory");
        }
    }

    public void lsa(Path currentDirectory) {
        // `ls -a` - lists all files, including hidden files
        try (Stream<Path> paths = Files.list(currentDirectory)) {
            paths.forEach(path -> {
                if (Files.isDirectory(path)) {
                    System.out.println(path.getFileName() + "/");
                } else {
                    System.out.println(path.getFileName());
                }
            });
        } catch (IOException e) {
            System.out.println("ls -a: Error reading directory");
        }
    }

    public void lsr(Path currentDirectory) {
        // `ls -r` - recursively lists all files and directories
        try {
            Files.walk(currentDirectory).forEach(path -> {
                if (Files.isDirectory(path)) {
                    System.out.println(path + "/");
                } else {
                    System.out.println("  " + path);
                }
            });
        } catch (IOException e) {
            System.out.println("ls -r: Error reading directory");
        }
    }

    public void mkdir(String[] command, Path currentDirectory) {
        // Check if at least one directory name argument is provided
        if (command.length < 2) {
            System.out.println("mkdir: Missing directory argument (name)");
            return; // Exit if no directory names are given
        }

        // Loop over each directory name given as an argument
        for (int i = 1; i < command.length; i++) {
            String dirName = command[i];
            Path dirPath = currentDirectory.resolve(dirName);

            // Check if the parent directory exists
            if (!Files.exists(currentDirectory)) {
                System.out.println("mkdir: Parent directory does not exist for '" + dirName + "'");
                continue; // Skip to the next directory name if the parent directory doesn't exist
            }

            try {
                // Attempt to create the directory
                Files.createDirectory(dirPath);
                System.out.println("Directory created: " + dirPath);
            } catch (FileAlreadyExistsException e) {
                // Handle case where the directory already exists
                System.out.println("mkdir: Failed to create directory '" + dirName + "': A directory with the same name already exists.");
            } catch (IOException e) {
                // Handle any other I/O errors during directory creation
                System.out.println("mkdir: An error occurred while creating the directory '" + dirName + "'.");
            }
        }
    }


    public void rmdir(String[] command, Path currentDirectory) {
        // Check if the directory argument is provided
        if (command.length < 2) {
            System.out.println("rmdir: Missing directory argument (name)");
            return; // Exit if no directory name is given
        }

        // Resolve the directory path to delete
        Path dirToDelete = currentDirectory.resolve(command[1]).normalize();

        // Check if the directory exists
        if (Files.notExists(dirToDelete)) {
            System.out.println("rmdir: Directory does not exist: '" + command[1] + "'");
            return; // Exit if the directory doesn't exist
        }

        // Check if the path is actually a directory
        if (!Files.isDirectory(dirToDelete)) {
            System.out.println("rmdir: Not a directory: '" + command[1] + "'");
            return; // Exit if the path is not a directory
        }

        try {
            // Attempt to delete the directory (only if it's empty)
            Files.delete(dirToDelete);
            System.out.println("Directory deleted: " + dirToDelete);
        } catch (DirectoryNotEmptyException e) {
            // Handle case where the directory is not empty
            System.out.println("rmdir: Directory is not empty: " + command[1]);
        } catch (IOException e) {
            // Handle any other I/O errors during deletion
            System.out.println("rmdir: Error deleting directory: " + command[1]);
        }
    }

    public void cat(String[] command, Path currentDirectory) {
        if (command.length < 2) {
            System.out.println("cat: Missing file argument");
            return;
        }
        Path filePath = currentDirectory.resolve(command[1]);
        try {
            Files.lines(filePath).forEach(System.out::println);
        } catch (IOException e) {
            System.out.println("cat: Failed to read file " + command[1]);
        }
    }

    public void touch(String fileName, Path currentDirectory) {
        Path filePath = currentDirectory.resolve(fileName);
        try {
            if (Files.exists(filePath)) {
                System.out.println("File already exists: " + fileName);
            } else {
                Files.createFile(filePath);
                System.out.println("File created: " + fileName);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file: " + fileName);
            e.printStackTrace();
        }
    }

    public void rm(String fileName, Path currentDirectory) {
        Path path = currentDirectory.resolve(fileName);
        if (Files.notExists(path)) {
            System.out.println("File or directory does not exist: " + fileName);
            return;
        }

        try {
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
                System.out.println("Directory and its contents deleted successfully: " + fileName);
            } else {
                Files.delete(path);
                System.out.println("File deleted successfully: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete: " + fileName + " - " + e.getMessage());
        }
    }

    public void redirect(String input) {
        String[] parts;
        boolean append = input.contains(">>");

        if (append) {
            parts = input.split(">>");
        } else {
            parts = input.split(">");
        }

        if (parts.length < 2) {
            System.out.println("Invalid command format: " + input);
            return;
        }

        String command = parts[0].trim();
        String fileName = parts[1].trim();
        Path filePath = Paths.get(fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile(), append))) {
            if (command.startsWith("echo")) {
                String message = command.substring(5).trim();
                writer.println(message);
                System.out.println("Message written to file: " + fileName);
            } else {
                System.out.println("Unknown command: " + command);
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public void pwd(Path currentDirectory) {
        System.out.println(currentDirectory);
    }

    public Path cd(String[] command, Path currentDirectory) {
        if (command.length < 2) {
            System.out.println("cd: Missing directory argument");
            return currentDirectory;
        }
        Path newPath = currentDirectory.resolve(command[1]).normalize();
        if (Files.isDirectory(newPath)) {
            return newPath;
        } else {
            System.out.println("cd: No such directory: " + command[1]);
            return currentDirectory;
        }
    }


    public void mv(String[] command, Path currentDirectory) {
        // Check if both source and destination arguments are provided
        if (command.length < 3) {
            System.out.println("mv: Missing source or destination argument");
            return;
        }

        // Resolve source and destination paths relative to the current directory
        Path sourcePath = currentDirectory.resolve(command[1]);
        Path destinationPath = currentDirectory.resolve(command[2]);

        // Check if the source file exists
        if (!Files.exists(sourcePath)) {
            System.out.println("mv: Source file or directory does not exist: " + command[1]);
            return;
        }

        // If destination is a directory, move the source into the destination directory
        if (Files.isDirectory(destinationPath)) {
            destinationPath = destinationPath.resolve(sourcePath.getFileName());
        }

        try {
            // Attempt to move the file or directory
            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Moved " + sourcePath.getFileName() + " to " + destinationPath);
        } catch (IOException e) {
            System.out.println("mv: Failed to move " + command[1] + " to " + command[2]);
        }
    }


    public void grep(String[] command, Path currentDirectory) {
        if (command.length < 3) {
            System.out.println("grep: Missing pattern or file argument");
            return;
        }
        String pattern = command[1];
        Path filePath = currentDirectory.resolve(command[2]);

        try (Stream<String> lines = Files.lines(filePath)) {
            lines.filter(line -> line.contains(pattern))
                    .forEach(System.out::println);
        } catch (IOException e) {
            System.out.println("grep: Failed to read file " + command[2]);
        }
    }

}
