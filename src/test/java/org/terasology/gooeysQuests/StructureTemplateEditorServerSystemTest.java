/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gooeysQuests;

import org.junit.Test;
import org.terasology.gooeysQuests.api.Region;
import org.terasology.gooeysQuests.api.SpawnBlockRegionsComponent.RegionToFill;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class StructureTemplateEditorServerSystemTest {



    @Test
    public void testMergeRegionsByXTrippleMerge() {
        String blockA = "a";

        // Region A and B can be merged
        RegionToFill regionA = createRegion(blockA, 2, 0, -1, 2, 1, 5);
        RegionToFill regionB = createRegion(blockA, 3, 0, -1, 3, 1, 5);
        RegionToFill regionC = createRegion(blockA, 4, 0, -1, 6, 1, 5);

        // Region A and B are not behind each other to test that the algotihm can deal with it:
        List<RegionToFill> regions = new ArrayList<>();
        regions.add(copyOf(regionA));
        regions.add(copyOf(regionC));
        regions.add(copyOf(regionB));

        StructureTemplateEditorServerSystem.mergeRegionsByX(regions);
        RegionToFill regionAB = createRegion(blockA, 2, 0, -1, 6, 1, 5);

        List<RegionToFill> expectedRegions = new ArrayList<>();
        expectedRegions.add(regionAB);
        assertRegionListsEqual(expectedRegions, regions);
    }


    @Test
    public void testMergeRegionsByXWithTooFarAwayCase() {
        String blockA = "a";

        RegionToFill regionA = createRegion(blockA, 2, 0, -1, 2, 1, 5);
        RegionToFill regionB = createRegion(blockA, 4, 0, -1, 3, 1, 5);

        // Region A and B are not behind each other to test that the algotihm can deal with it:
        List<RegionToFill> regions = new ArrayList<>();
        regions.add(copyOf(regionA));
        regions.add(copyOf(regionB));

        StructureTemplateEditorServerSystem.mergeRegionsByX(regions);

        List<RegionToFill> expectedRegions = new ArrayList<>();
        expectedRegions.add(regionA);
        expectedRegions.add(regionB);
        assertRegionListsEqual(expectedRegions, regions);
    }

    @Test
    public void testMergeRegionsByXWithMinYDifferCase() {
        String blockA = "a";

        RegionToFill regionA = createRegion(blockA, 2, 0, -1, 2, 1, 5);
        RegionToFill regionB = createRegion(blockA, 3, -1, -1, 3, 1, 5);

        List<RegionToFill> regions = new ArrayList<>();
        regions.add(copyOf(regionA));
        regions.add(copyOf(regionB));
        StructureTemplateEditorServerSystem.mergeRegionsByX(regions);

        List<RegionToFill> expectedRegions = new ArrayList<>();
        expectedRegions.add(regionA);
        expectedRegions.add(regionB);

        assertRegionListsEqual(expectedRegions, regions);
    }

    @Test
    public void testMergeRegionsByXWithMaxYDifferCase() {
        String blockA = "a";

        RegionToFill regionA = createRegion(blockA, 2, 0, -1, 2, 1, 5);
        RegionToFill regionB = createRegion(blockA, 3, 0, -1, 3, 2, 5);

        List<RegionToFill> regions = new ArrayList<>();
        regions.add(copyOf(regionA));
        regions.add(copyOf(regionB));
        StructureTemplateEditorServerSystem.mergeRegionsByX(regions);

        List<RegionToFill> expectedRegions = new ArrayList<>();
        expectedRegions.add(regionA);
        expectedRegions.add(regionB);

        assertRegionListsEqual(expectedRegions, regions);
    }

    @Test
    public void testMergeRegionsByXWithMinZDifferCase() {
        String blockA = "a";

        RegionToFill regionA = createRegion(blockA, 2, 0, -1, 2, 1, 5);
        RegionToFill regionB = createRegion(blockA, 3, 0, -2, 3, 1, 5);

        List<RegionToFill> regions = new ArrayList<>();
        regions.add(copyOf(regionA));
        regions.add(copyOf(regionB));
        StructureTemplateEditorServerSystem.mergeRegionsByX(regions);

        List<RegionToFill> expectedRegions = new ArrayList<>();
        expectedRegions.add(regionA);
        expectedRegions.add(regionB);

        assertRegionListsEqual(expectedRegions, regions);
    }

    @Test
    public void testMergeRegionsByXWithMaxZDifferCase() {
        String blockA = "a";

        RegionToFill regionA = createRegion(blockA, 2, 0, -1, 2, 1, 5);
        RegionToFill regionB = createRegion(blockA, 3, 0, -1, 3, 1, 6);

        List<RegionToFill> regions = new ArrayList<>();
        regions.add(copyOf(regionA));
        regions.add(copyOf(regionB));
        StructureTemplateEditorServerSystem.mergeRegionsByX(regions);

        List<RegionToFill> expectedRegions = new ArrayList<>();
        expectedRegions.add(regionA);
        expectedRegions.add(regionB);

        assertRegionListsEqual(expectedRegions, regions);
    }

    @Test
    public void testMergeRegionsByXWithBlockTypeDifferCase() {
        String blockA = "a";
        String blockB = "b";

        RegionToFill regionA = createRegion(blockA, 2, 0, -1, 2, 1, 5);
        RegionToFill regionB = createRegion(blockB, 3, 0, -1, 3, 1, 5);

        List<RegionToFill> regions = new ArrayList<>();
        regions.add(copyOf(regionA));
        regions.add(copyOf(regionB));
        StructureTemplateEditorServerSystem.mergeRegionsByX(regions);

        List<RegionToFill> expectedRegions = new ArrayList<>();
        expectedRegions.add(regionA);
        expectedRegions.add(regionB);

        assertRegionListsEqual(expectedRegions, regions);
    }


    @Test
    public void testMergeRegionsByXWithBasicSuccessCase() {
        String blockA = "a";
        List<RegionToFill> regions = new ArrayList<>();

        // Region A and B can be merged
        RegionToFill regionA = createRegion(blockA, 2, 0, 1, 3, -1, 5);
        RegionToFill regionB = createRegion(blockA, 4, 0, 1, 5, -1, 5);

        // Region C is x wise inbetween but y wise different
        RegionToFill regionC = createRegion(blockA, 3, -1, 1, 3, -1, 5);

        // Region D is x wise in between but z wise different
        RegionToFill regionD = createRegion(blockA, 3, 0, 2, 3, -1, 5);


        // Region A and B are not behind each other to test that the algotihm can deal with it:
        regions.add(copyOf(regionA));
        regions.add(copyOf(regionC));
        regions.add(copyOf(regionD));
        regions.add(copyOf(regionB));


        StructureTemplateEditorServerSystem.mergeRegionsByX(regions);

        RegionToFill regionAB = createRegion(blockA, 2, 0, 1, 5, -1, 5);


        List<RegionToFill> expectedRegions = new ArrayList<>();
        expectedRegions.add(regionC);
        expectedRegions.add(regionAB);
        expectedRegions.add(regionD);
        assertRegionListsEqual(expectedRegions, regions);
    }

    private RegionToFill copyOf(RegionToFill r) {
        return createRegion(r.blockType, r.region.min.x, r.region.min.y, r.region.min.z, r.region.max.x, r.region.max.y,
                r.region.max.z);
    }

    private RegionToFill createRegion(String blockType, int minX, int minY, int minZ,
                                                                 int maxX, int maxY, int maxZ) {
        RegionToFill r = new RegionToFill();
        r.region = new Region();
        r.region.min.set(minX, minY, minZ);
        r.region.max.set(maxX, maxY, maxZ);
        r.blockType = blockType;
        return r;
    }

    private static String regionToString(RegionToFill r) {
        return String.format("block: \"%s\", min: [%d, %d, %d], max: [%d, %d, %d]", r.blockType, r.region.min.x,
                r. region.min.y, r. region.min.z, r. region.max.x, r. region.max.y, r. region.max.z);
    }

    private static String regionsToString(List<RegionToFill> regions) {
        StringBuilder sb = new StringBuilder();
        for (RegionToFill r: regions) {
            sb.append(regionToString(r));
            sb.append("\n");
        }
        return sb.toString();
    }

    private static void assertRegionListsEqual(List<RegionToFill> expected, List<RegionToFill> actual) {
        assertEquals(regionsToString(expected), regionsToString(actual));
    }
}
