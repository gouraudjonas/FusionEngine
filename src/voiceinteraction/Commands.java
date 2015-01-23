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
            restartTimer(commands, bus);

            if (findWord(strings, "vert")) {
                commands.add("couleur vert");
            } else if (findWord(strings, "rouge")) {
                commands.add("couleur rouge");
            } else if (findWord(strings, "bleu")) {
                commands.add("couleur bleu");
            }
        }

        // Get an place
        if (findWord(strings, "Emplacement")) {
            restartTimer(commands, bus);

            if (findWord(strings, "ici")) {
                commands.add("emplacement ici");
            }

        }

        // Get a mouse click
        if (findWord(strings, "MousePressed")) {
            restartTimer(commands, bus);

            int x = 0, y = 0;

            // Get numbers and equal signs
            String copy = strings[0].replaceAll("[([a-z]|[A-Z]|:| )]", "");
            // Get numbers
            String[] numbers = copy.split("=");
            x = Integer.parseInt(numbers[1]);
            y = Integer.parseInt(numbers[2]);

            commands.add("mousepressed");
            commands.add(Integer.toString(x));
            commands.add(Integer.toString(y));
        }

        // Get a clean
        if (findWord(strings, "cline")) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
            executeCommands(commands, bus);
        }
    }

    public static void restartTimer(ArrayList<String> commands, Ivy bus) {
        timer.cancel();
        timer.purge();
        timer = new Timer();
        setTimerTask(commands, bus);
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
        System.out.print("DEBUG > inside commands ");
        commands.stream().forEach((command) -> {
            System.out.print(" - " + command);
        });
        System.out.println();

        if (commands.isEmpty()) {
            return;
        }
        try {
            if (findWord(commands.get(0), "creation")) {
                // Creation of a rectangle
                if (findWord(commands.get(0), "rectangle")) {
                    if (commands.size() == 1) {
                        bus.sendMsg("Palette:CreerRectangle");
                    } else if (findWord(commands.get(1), "couleur")) {

                        if (findWord(commands.get(1), "rouge")) {
                            if (commands.size() >= 3) {
                                if (findWord(commands.get(2), "ici")) {
                                    if (commands.size() >= 6 && findWord(commands.get(3), "mousepressed")) {
                                        // x and y are in a different String than mousepressed, easier to store and take back
                                        bus.sendMsg("Palette:CreerRectangle x=" + commands.get(4) + " y=" + commands.get(5) + " couleurFond=255:0:0 couleurContour=255:0:0");
                                    } else {
                                        bus.sendMsg("Palette:CreerRectangle  couleurFond=255:0:0 couleurContour=255:0:0");
                                    }
                                }
                            } else {
                                bus.sendMsg("Palette:CreerRectangle couleurFond=255:0:0 couleurContour=255:0:0");
                            }
                        } else if (findWord(commands.get(1), "vert")) {
                            bus.sendMsg("Palette:CreerRectangle couleurFond=0:255:0 couleurContour=0:255:0");
                        } else if (findWord(commands.get(1), "bleu")) {
                            bus.sendMsg("Palette:CreerRectangle couleurFond=0:0:255 couleurContour=0:0:255");
                        }

                    } else if (findWord(commands.get(1), "ici")) {
                        if (commands.size() >= 5 && findWord(commands.get(2), "mousepressed")) {
                            // x and y are in a different String than mousepressed, easier to store and take back
                            bus.sendMsg("Palette:CreerRectangle x=" + commands.get(3) + " y=" + commands.get(4));
                        } else {
                            bus.sendMsg("Palette:CreerRectangle");
                        }
                    }

                    // Creation of an ellipse
                } else if (findWord(commands.get(0), "ellipse")) {
                    if (commands.size() == 1) {
                        bus.sendMsg("Palette:CreerEllipse");
                    } else if (findWord(commands.get(1), "couleur")) {
                        if (findWord(commands.get(1), "rouge")) {
                            bus.sendMsg("Palette:CreerEllipse couleurFond=255:0:0 couleurContour=255:0:0");
                        } else if (findWord(commands.get(1), "vert")) {
                            bus.sendMsg("Palette:CreerEllipse couleurFond=0:255:0 couleurContour=0:255:0");
                        } else if (findWord(commands.get(1), "bleu")) {
                            bus.sendMsg("Palette:CreerEllipse couleurFond=0:0:255 couleurContour=0:0:255");
                        }
                    } else if (findWord(commands.get(1), "ici")) {
                        if (commands.size() >= 5 && findWord(commands.get(2), "mousepressed")) {
                            bus.sendMsg("Palette:CreerEllipse x=" + commands.get(3) + " y=" + commands.get(4));
                        } else {
                            bus.sendMsg("Palette:CreerEllipse");
                        }
                    }
                }
            }
        } catch (IvyException ex) {
            Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
        }

        commands.clear();
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
