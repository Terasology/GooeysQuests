#  Gooey's Quests

A module for Terasology.

## How to play?

First you have to start the game with GooeysQuests activated and in a Perlin world. When the game is created, you will see a character named Gooey that will ask you something. Depending on your answer, Gooey will change the world adding some structures. Now there are two available structures, the dungeon and the dwarf hall, but they have diferents combinations, so every time gooey add the structure, you will see something new. But be careful, maybe you don’t like these changes.

## How to add more structures?

If you want to add a new structure, first you have to create the structures templates that will form the structure. Then you will have to create at least two java class GooeysQuests /src/ main/ java/ org/ terasology/ gooeysQuests/ quests. One will be the component and the other will be the system that allow Gooey to spawn your structure.
Name conventions:
•	Add the name of your structure at the beggining of the name of each structure template that you create
•	The format for java class names is: (Yourstructure)QuestComponent and (Yourstructure)QuestSystem

## Credits

A drawing of a "gooey" made by SuperSnark was the inspiration for Gooey main character of this module.
