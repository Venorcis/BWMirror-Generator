package bwapi;

import java.io.*;
import java.io.File;
import java.lang.Exception;
import java.lang.UnsupportedOperationException;
import java.util.*;
import java.util.regex.Pattern;

public class Mirror {

    public static final String LOG_TAG = "[BWMirror] ";

    private Game game;

    private AIModule module = new AIModule();

    private static void extractResourceFile(String resourceFilename, String outputFilename) throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceFilename);
        if (in == null)
            throw new FileNotFoundException("Resource file not found: " + resourceFilename);
        FileOutputStream out;
        try {
            out = new FileOutputStream(outputFilename);
        } catch (Exception e) {
            throw new FileNotFoundException("Could not open output file: " + outputFilename);
        }
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        in.close();
    }

    private static boolean extractAndLoadNativeLibraries() {
        try {
            System.out.println(LOG_TAG + "Extracting bwapi_bridge.dll");
            extractResourceFile("bwapi_bridge.dll", "./bwapi_bridge.dll");

            System.out.println(LOG_TAG + "Extracting libgmp-10.dll");
            extractResourceFile("libgmp-10.dll", "./libgmp-10.dll");

            System.out.println(LOG_TAG + "Extracting libmpfr-4.dll");
            extractResourceFile("libmpfr-4.dll", "./libmpfr-4.dll");

            System.out.println(LOG_TAG + "Loading native library bwapi_bridge.dll");
            System.load(new File("./bwapi_bridge.dll").getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static boolean extractBwtaDataFiles() {
        try {
            Collection<String> bwtaFilenames = ResourceList.getResources(Pattern.compile("bwapi\\-data/BWTA2/[a-zA-Z0-9]+\\.bwta"));

            System.out.println(LOG_TAG + "Creating ./bwapi-data/BWTA2 directory");
            new File("./bwapi-data/BWTA2").mkdirs();

            for (String filename : bwtaFilenames) {
                System.out.println(LOG_TAG + "Extracting " + filename);
                String outputFilename = "./" + filename;
                extractResourceFile(filename, outputFilename);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean is64BitJRE() {
        String bits = System.getProperty("sun.arch.data.model");
        if (bits == null)
            return System.getProperty("java.vm.name").contains("64");
        else
            return bits.equals("64");
    }

    static {
        if (is64BitJRE())
            throw new UnsupportedOperationException("BWMirror must be run on a 32-bit JRE/JDK.");

        if (!extractAndLoadNativeLibraries())
            System.exit(1);
        if (!extractBwtaDataFiles())
            System.exit(1);

        System.out.println(LOG_TAG + "Initializing constants tables.");
        initTables();
    }

    public Game getGame() {
        return game;
    }

    public AIModule getModule() {
        return module;
    }

    /**
     * Initializes all BWAPI constant lookup tables.
     */
    private static native void initTables();

    /**
     * Initializes a connection to Broodwar, initializes the a {@link Game} object, and dispatches
     * events to your listener as long as Broodwar is in a game. Will automatically attempt to 
     * reconnect to Broodwar if the connection is lost at any point. If this method is called 
     * before Broodwar is running, will wait until an initial connection can be established.
     *
     * The {@link Game} instance returned by {@link #getGame()} is only valid while this method
     * is running. If your code holds a copy of this object anywhere else, do not try to use it
     * again after this method returns.
     *
     * @param returnOnMatchEnd
     *        If true, will disconnect from Broodwar and return after the first match ends
     *        (regardless of how it ended). You can call {@link #startGame} again to run another
     *        match as needed.
     *        If false, will run an infinite loop allowing you to keep your bot running as many
     *        subsequent matches as desired.
     */
    public void startGame(boolean returnOnMatchEnd) {
        System.out.println(LOG_TAG + "Connecting to Broodwar...");
        if (reconnect())
            System.out.println(LOG_TAG + "Connection successful, starting match...");
        else {
            System.out.println(LOG_TAG + "Connection attempt aborted.");
            return;
        }

        game = getInternalGame();

        boolean inGame = game.isInGame();
        boolean previouslyInGame = inGame;
        if (inGame)
            System.out.println(LOG_TAG + "Match already running.");

        while (true) {
            if (Thread.interrupted()) {
                System.out.println(LOG_TAG + "Interrupted.");
                break;
            }

            if (!inGame) {
                if (previouslyInGame) {
                    System.out.println(LOG_TAG + "Match ended.");
                    if (returnOnMatchEnd)
                        break;
                }

                update();

            } else {
                if (!previouslyInGame)
                    System.out.println(LOG_TAG + "Game ready!!!");

                processGameEvents();
                update();
            }

            if (!isConnected()) {
                System.out.println(LOG_TAG + "Reconnecting...");
                reconnect();
            }

            previouslyInGame = inGame;
            inGame = game.isInGame();
        }

        System.out.println(LOG_TAG + "Finished.");
        System.out.println(LOG_TAG + "Disconnecting from Broodwar...");

        if (isConnected())
            disconnect();

        game = null;

        System.out.println(LOG_TAG + "Returning...");
    }

    private boolean reconnect() {
        while (!connect()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }

	/**
	 * Returns the current connection state to a running Broodwar instance.
	 */
    public static native boolean isConnected();

    private static native boolean connect();

    private static native void disconnect();

    private static native void update();

    private native Game getInternalGame();

    private native void processGameEvents();
}
