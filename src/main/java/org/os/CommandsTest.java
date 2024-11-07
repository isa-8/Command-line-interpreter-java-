package org.os;
import org.os.Commands;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.nio.file.Paths;
import java.io.*;



public class CommandsTest {

    private Commands commands;
    private Path testDirectory;
    @BeforeEach
    void setUp() throws Exception {
        // Initialize CLICommands instance and set up a temporary directory for tests
        commands = new Commands(); // Ensure this is instantiated
        testDirectory = Files.createTempDirectory("testDir"); // Create a temp directory
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up by deleting the test directory
        if (Files.exists(testDirectory)) {
            Files.walk(testDirectory)
                    .sorted((a, b) -> b.compareTo(a)) // Delete child paths before parents
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            System.out.println("Error cleaning up test directory: " + e.getMessage());
                        }
                    });
        }
    }
    @Test
    void testMkdirCreatesDirectory() throws Exception {
        String[] mkdirCommand = {"mkdir", "newDir"};

        // Act
        commands.mkdir(mkdirCommand, testDirectory);  // Use the instance to call the method

        // Assert
        assertTrue(Files.exists(testDirectory.resolve("newDir")), "Directory should be created");
    }

    @Test
    void testMkdirMissingDirectoryArgument() throws Exception {
        String[] mkdirCommand = {"mkdir"}; // Missing directory name

        // Act
        commands.mkdir(mkdirCommand, testDirectory); // Call the method

        // Assert: Check that no directories were created
        assertEquals(0, Files.list(testDirectory).count(), "No directory should be created");
    }

    @Test
    void testMkdirInNonExistentPath() throws Exception {
        String[] mkdirCommand = {"mkdir", "nonExistentDir"};

        // Act: Attempt to create a directory in a non-existent path
        Path invalidPath = testDirectory.resolve("invalidPath");
        commands.mkdir(mkdirCommand, invalidPath); // Invalid path

        // Assert: Check that the directory was not created
        assertFalse(Files.exists(invalidPath.resolve("nonExistentDir")), "Directory should not be created in a non-existent path");
    }



    @Test
    void testMkdirDoesNotCreateExistingDirectory() throws Exception {
        String[] mkdirCommand = {"mkdir", "existingDir"};

        // Create the directory first
        commands.mkdir(mkdirCommand, testDirectory); // Use the instance

        // Try creating it again and capture output
        commands.mkdir(mkdirCommand, testDirectory); // Use the instance

        // Assert: Check that the directory still exists and only one directory was created
        assertEquals(1, Files.list(testDirectory).count(), "Directory should not create a second instance because it already exists");
    }

    @Test
    void testRmdirMissingDirectoryArgument() {
        String[] rmdirCommand = {"rmdir"}; // Missing directory name

        // Act: Call the method
        commands.rmdir(rmdirCommand, testDirectory);
        // Verify output manually since we're not capturing console output
    }

    @Test
    void testRmdirNonExistentDirectory() {
        String[] rmdirCommand = {"rmdir", "nonExistentDir"};

        // Act: Call the method
        commands.rmdir(rmdirCommand, testDirectory);
        // Verify output manually since we're not capturing console output
    }

    @Test
    void testRmdirNonDirectoryPath() throws Exception {
        String[] rmdirCommand = {"rmdir", "file.txt"};

        // Create a file to test non-directory path
        Files.createFile(testDirectory.resolve("file.txt"));

        // Act: Call the method
        commands.rmdir(rmdirCommand, testDirectory);

        // Clean up: Delete the file after the test
        Files.deleteIfExists(testDirectory.resolve("file.txt"));
    }

    @Test
    void testRmdirSuccessfulDeletion() throws Exception {
        String[] rmdirCommand = {"rmdir", "emptyDir"};

        // Create an empty directory for the test
        Files.createDirectory(testDirectory.resolve("emptyDir"));

        // Act: Call the method
        commands.rmdir(rmdirCommand, testDirectory);

        // Assert: Check that the directory no longer exists
        assertFalse(Files.exists(testDirectory.resolve("emptyDir")), "The empty directory should be deleted.");
    }

    @Test
    void testRmdirNotEmptyDirectory() throws Exception {
        String[] rmdirCommand = {"rmdir", "notEmptyDir"};

        // Create a directory and a file inside it
        Path notEmptyDir = testDirectory.resolve("notEmptyDir");
        Files.createDirectory(notEmptyDir);
        Files.createFile(notEmptyDir.resolve("file.txt")); // Create a file inside the directory

        // Act: Call the method
        commands.rmdir(rmdirCommand, testDirectory);

        // Assert: Check that the directory still exists
        assertTrue(Files.exists(notEmptyDir), "The non-empty directory should not be deleted.");
    }

    /////////////////////////////////////////////////////////////
    @Test
    void testPwdDisplaysCurrentDirectory() {
        // Test to display the current directory
        commands.pwd(testDirectory);
    }

    @Test
    void testCdChangesDirectory() throws IOException {
        // Create a new directory and test changing to it
        Path newDir = Files.createDirectory(testDirectory.resolve("newDir"));
        String[] cdCommand = {"cd", "newDir"};

        // Change directory and assert it changed successfully
        Path result = commands.cd(cdCommand, testDirectory);
        assertEquals(newDir, result, "Should change to the specified directory");
    }

    @Test
    void testCdNonExistentDirectory() {
        // Attempt to change to a non-existent directory
        String[] cdCommand = {"cd", "nonExistentDir"};
        Path result = commands.cd(cdCommand, testDirectory);

        // Assert that the current directory remains unchanged
        assertEquals(testDirectory, result, "Should remain in the current directory if target does not exist");
    }

    @Test
    void testMvMovesFileSuccessfully() throws IOException {
        // Create a file to move
        Path fileToMove = Files.createFile(testDirectory.resolve("file.txt"));
        Path targetDir = Files.createDirectory(testDirectory.resolve("targetDir"));
        String[] mvCommand = {"mv", "file.txt", "targetDir/file.txt"};

        // Move the file and assert successful movement
        commands.mv(mvCommand, testDirectory);
        assertTrue(Files.exists(targetDir.resolve("file.txt")), "File should be moved to the target directory");
        assertFalse(Files.exists(fileToMove), "File should no longer exist in the original location");
    }

    @Test
    void testMvNonExistentSourceFile() {
        // Attempt to move a non-existent file
        String[] mvCommand = {"mv", "nonExistentFile.txt", "newLocation.txt"};

        // Call the move command (no assertion needed, but we can log the outcome if necessary)
        commands.mv(mvCommand, testDirectory);
        // It's often good to assert that no exceptions were thrown, but we can validate if needed.
    }

    @Test
    void testGrepFindsPatternInFile() throws IOException {
        // Create a file with known content
        Path file = Files.createFile(testDirectory.resolve("sample.txt"));
        Files.write(file, List.of("Hello World", "Java Programming", "Pattern Match Test"));

        // Search for a pattern in the file
        String[] grepCommand = {"grep", "Java", "sample.txt"};
        commands.grep(grepCommand, testDirectory);
        // You can add assertions for expected output if needed
    }

    @Test
    void testGrepPatternNotFoundInFile() throws IOException {
        // Create a file without the target pattern
        Path file = Files.createFile(testDirectory.resolve("sample.txt"));
        Files.write(file, List.of("Hello World", "Testing CLI Commands", "No Match Here"));

        // Search for a pattern that doesn't exist
        String[] grepCommand = {"grep", "Java", "sample.txt"};
        commands.grep(grepCommand, testDirectory);
        // You can add assertions for expected output if needed
    }

    @Test
    void testGrepNonExistentFile() {
        // Attempt to grep in a non-existent file
        String[] grepCommand = {"grep", "pattern", "nonExistentFile.txt"};
        commands.grep(grepCommand, testDirectory);
        // You can add assertions for expected output if needed
    }


    /////////////////////////////////////////////////////////////

    @Test
    void testTouchCase1() {
        String s = "test22";
        commands.touch(s, testDirectory);
        assertTrue(Files.exists(testDirectory.resolve(s))); // Check in current directory
    }

    // Test case for attempting to create a file that already exists
    @Test
    void testTouchCase2() {
        String s = "test22";
        commands.touch(s, testDirectory);
        assertTrue(Files.exists(testDirectory.resolve(s)));

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        System.setOut(new PrintStream(content));

        commands.touch(s, testDirectory); // Try to create again
        assertTrue(content.toString().contains("File already exists"));
    }

    // Test case for removing a file
    @Test
    void testRmCase1() {
        String s = "test21";
        commands.touch(s, testDirectory);
        commands.rm(s, testDirectory);
        assertFalse(Files.exists(testDirectory.resolve(s))); // Check in current directory
    }

    // Test case for removing a file that does not exist
    @Test
    void testRmCase2() {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        System.setOut(new PrintStream(content));

        commands.rm("assdsa.txt", testDirectory); // File does not exist
        assertTrue(content.toString().contains("File or directory does not exist"));
    }

    // Test case for redirecting output to a file using >
    @Test
    public void testRedirectCase1() throws IOException {
        String input = "echo ddkjk > testFile.txt";
        commands.redirect(input); // Pass the current directory
        Path path = Paths.get("testFile.txt");
        assertTrue(Files.exists(path));

        String content = Files.readString(path).trim();
        assertEquals("ddkjk", content);
    }

    // Test case for appending output to a file using >>
    @Test
    public void testRedirectCase2() throws IOException {
        String input1 = "echo ddkjk > testFile.txt"; // Create file first
        commands.redirect(input1);
        String input2 = "echo aaaa >> testFile.txt"; // Append to the file
        commands.redirect(input2);

        Path path = Paths.get("testFile.txt");
        assertTrue(Files.exists(path));

        String content = Files.readString(path).trim();
        assertTrue(content.contains("ddkjk"));
        assertTrue(content.contains("aaaa"));
    }

    // Test case for handling an unknown command during redirection
    @Test
    public void testRedirectCase3() throws IOException {
        String input = "bhgh aaaa >> testFile.txt";

        ByteArrayOutputStream content = new ByteArrayOutputStream();
        System.setOut(new PrintStream(content));
        commands.redirect(input);
        assertTrue(content.toString().contains("Unknown command:"));
    }

    @Test
    void testLsDisplaysVisibleFiles() throws IOException {
        String[] lsCommand = {"ls"};
        Files.createFile(testDirectory.resolve("visibleFile.txt"));
        Files.createFile(testDirectory.resolve(".hiddenFile"));
        Files.createDirectory(testDirectory.resolve("subDirectory"));
        commands.ls(testDirectory);
    }

    @Test
    void testLsEmptyDirectory() throws IOException {
        String[] lsCommand = {"ls"};
        commands.ls(testDirectory);
    }

    @Test
    void testCatDisplaysFileContent() throws IOException {
        String[] catCommand = {"cat", "file.txt"};
        Path file = testDirectory.resolve("file.txt");
        Files.write(file, List.of("This is a test file."));
        commands.cat(catCommand, testDirectory);
    }

    @Test
    void testCatEmptyFile() throws IOException {
        String[] catCommand = {"cat", "emptyFile.txt"};
        Path emptyFile = testDirectory.resolve("emptyFile.txt");
        Files.createFile(emptyFile);
        commands.cat(catCommand, testDirectory);
    }

    @Test
    void testCatNonexistentFile() {
        String[] catCommand = {"cat", "nonexistentFile.txt"};
        commands.cat(catCommand, testDirectory);
    }





}

