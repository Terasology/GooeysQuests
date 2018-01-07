#  Gooey's Quests

A module for Terasology.

## Getting Started

•	You have to start the game with GooeysQuests activated and in a Perlin world.
•	Walk around until you find a character named Gooey that will ask you something.
•	Depending on your answer, Gooey will change the world adding some structures.
•	Right now there are two available structures, the dungeon and the dwarf hall, but they have different combinations, so every time gooey add the structure, you will see something new.
•	But be careful, maybe you don’t like these changes.

## Adding more structures

If you want to add a new structure, first you have to create the structures templates that will form the structure. Then you will have to create two java classes: a component and a system, typically in the `folder /src/main/java/org/terasology/gooeysQuests/quests`
Naming conventions:
•	Add the name of your structure at the beginning of the name of each structure template that you create
•	The format for java class names is: <YourStructure>QuestComponent and <YourStructure>QuestSystem

## Credits

A drawing of a "gooey" made by SuperSnark was the inspiration for Gooey, the main character of this module.
