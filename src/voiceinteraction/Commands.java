/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voiceinteraction;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.irit.elipse.enseignement.isia.PaletteGraphique;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sacapuce
 */
public class Commands {

    private static boolean isThereAHergerFrame = false;
    private static boolean isThereAPaletteFrame = false;
    private static final String address = "127.255.255:2010";
    private static Timer timer = new Timer();

    public static void commandsGeneration(IvyClient ic, String[] strings,
            ArrayList<String> commands, Ivy bus) {
        if (findWord(strings, "initialiser")) {
            // Initiliaze Palette
            if (!isThereAPaletteFrame && findWord(strings, "palette")) {
                PaletteGraphique myPG = new PaletteGraphique(address, 0, 0, 300, 300);
                isThereAPaletteFrame = true;
            }

            // Initialize Herger
            /*if (!isThereAHergerFrame && findWord(strings, "geste")) {
             myPG = new HergerUI(address, 0, 0, 300, 300);
             isThereAPaletteFrame = true;
             }*/
        }

        // Get a creation
        if (findWord(strings, "creation")) {
            // A creation is the first command
            executeCommands(commands, bus);
            commands = new ArrayList<>();

            if (findWord(strings, "rectangle")) {
                commands.add("creation rectangle");
                setTimerTask(commands, bus);
            } else if (findWord(strings, "ellipse")) {
                commands.add("creation ellipse");
                setTimerTask(commands, bus);
            }
        }

        // Get a color
        if (findWord(strings, "Couleur")) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
            setTimerTask(commands, bus);

            /*if (!commands.isEmpty() && findWord(commands.get(0), "couleur")) {
             commands = new ArrayList<>();
             }*/
            if (findWord(strings, "vert")) {
                commands.add("couleur vert");
            } else if (findWord(strings, "rouge")) {
                commands.add("couleur rouge");
            } else if (findWord(strings, "bleu")) {
                commands.add("couleur bleu");
            }
        }

        // Get a emplacement
        if (findWord(strings, "emplacement")) {
            // Can't be the first command, if it is then it's a false recognition
            if (!commands.isEmpty()) {
                if (findWord(strings, "ici")) {
                    commands.add("emplacement ici");
                }
            }
        }

        // Get a mouse click
        if (findWord(strings, "mousepressed")) {
            // Can't be the first command, if it is then it's a false recognition
            if (!commands.isEmpty()) {
                int x = 0, y = 0;
                boolean xDone = false;
                String[] result = strings[0].split(" ");
                for (String string : result) {
                    try {
                        if (!xDone) {
                            x = Integer.parseInt(string);
                            xDone = true;
                        } else {
                            y = Integer.parseInt(string);
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                commands.add("mousepressed " + x + " " + y);
            }
        }

        // Reset
        if (findWord(strings, "reset")) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
            commands.clear();
        }
    }

    public static void setTimerTask(ArrayList<String> commands, Ivy bus) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                executeCommands(commands, bus);
            }
        }, 2000);
    }

    public static void executeCommands(ArrayList<String> commands, Ivy bus) {
        System.out.print("DEBUG > commands.size ");
        for (String command : commands) {
            System.out.print(" - " + command);
        }
        System.out.println();

        if (commands.isEmpty()) {
            return;
        }
        try {
            if (findWord(commands.get(0), "creation")) {
                if (findWord(commands.get(0), "rectangle")) {
                    if (commands.size() == 1) {
                        bus.sendMsg("Palette:CreerRectangle");
                    } else if (findWord(commands.get(1), "couleur")) {
                        if (findWord(commands.get(1), "rouge")) {
                            bus.sendMsg("Palette:CreerRectangle couleurFond=255:0:0 couleurContour=255:0:0");
                        } else if (findWord(commands.get(1), "vert")) {
                            bus.sendMsg("Palette:CreerRectangle couleurFond=0:255:0 couleurContour=0:255:0");
                        } else if (findWord(commands.get(1), "bleu")) {
                            bus.sendMsg("Palette:CreerRectangle couleurFond=0:0:255 couleurContour=0:0:255");
                        }
                    }

                } else if (findWord(commands.get(0), "ellipse")) {
                    if (commands.size() == 1) {
                        bus.sendMsg("Palette:CreerEllipse");
                    }
                }
            }
        } catch (IvyException ex) {
            Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean findWord(String string, String word) {
        if (string.contains(word)) {
            return true;
        }

        return false;
    }

    public static boolean findWord(String[] strings, String word) {
        for (String string : strings) {
            if (string.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
