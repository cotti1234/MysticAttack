package de.cottisystem.zauberstab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import de.cottisystem.zauberstab.skilltree.PlayerSkillTree;
import de.cottisystem.zauberstab.skilltree.SkillTree;
import de.cottisystem.zauberstab.skilltree.SkillTreeNode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test-Suite für das Skill-Tree-System.
 */
public class SkillTreeTest {

    private SkillTree skillTree;
    private PlayerSkillTree playerSkillTree;

    @BeforeEach
    public void setUp() {
        skillTree = new SkillTree();
        playerSkillTree = new PlayerSkillTree();
    }

    @Test
    public void testSkillTreeNodeCreation() {
        SkillTreeNode node = new SkillTreeNode(
                "test_node", "test_ability", "Test", "Test Description",
                1, 2, 1, 5, null, 0, 0
        );

        assertEquals("test_node", node.getId());
        assertEquals("test_ability", node.getAbilityId());
        assertEquals(1, node.getRequiredLevel());
        assertEquals(2, node.getRequiredSkillPoints());
    }

    @Test
    public void testSkillTreeNodeUnlock() {
        SkillTreeNode node = new SkillTreeNode(
                "test_node", "test_ability", "Test", "Test Description",
                1, 2, 1, 5, null, 0, 0
        );

        assertTrue(node.canUnlock(1, 2), "Sollte freigeschaltet werden können");
        assertFalse(node.canUnlock(0, 2), "Sollte nicht freigeschaltet werden können (Level zu niedrig)");
        assertFalse(node.canUnlock(1, 1), "Sollte nicht freigeschaltet werden können (Punkte zu niedrig)");
    }

    @Test
    public void testPlayerSkillTree() {
        playerSkillTree.setLevel(5);
        playerSkillTree.setAvailableSkillPoints(10);

        assertEquals(5, playerSkillTree.getLevel());
        assertEquals(10, playerSkillTree.getAvailableSkillPoints());
    }

    @Test
    public void testPlayerSkillTreeUnlock() {
        playerSkillTree.setAvailableSkillPoints(5);

        assertTrue(playerSkillTree.unlockAbility("water_blast_node", 2));
        assertEquals(3, playerSkillTree.getAvailableSkillPoints());
        assertTrue(playerSkillTree.isUnlocked("water_blast_node"));
    }

    @Test
    public void testPlayerSkillTreeLevelUp() {
        playerSkillTree.setAvailableSkillPoints(5);
        playerSkillTree.unlockAbility("water_blast_node", 2);

        assertTrue(playerSkillTree.levelUpAbility("water_blast_node", 1));
        assertEquals(2, playerSkillTree.getAbilityLevel("water_blast_node"));
    }

    @Test
    public void testPlayerExperience() {
        playerSkillTree.setLevel(1);
        playerSkillTree.setExperience(0);

        assertFalse(playerSkillTree.addExperience(50), "Sollte nicht leveln");
        assertEquals(50, playerSkillTree.getExperience());

        assertTrue(playerSkillTree.addExperience(100), "Sollte leveln");
        assertEquals(2, playerSkillTree.getLevel());
    }

    @Test
    public void testSkillTreeIntegration() {
        skillTree.addNode(new SkillTreeNode(
                "water_blast_node", "water_blast", "Wasserstoß", "Ein kraftvoller Wasserstoß",
                1, 2, 1, 5, null, 0, 0
        ));

        assertTrue(skillTree.getNode("water_blast_node").isPresent());
        assertEquals(1, skillTree.getAllNodes().size());
    }

    @Test
    public void testSkillPointCalculation() {
        int level1Points = SkillTree.calculateSkillPoints(1);
        int level10Points = SkillTree.calculateSkillPoints(10);

        assertEquals(5, level1Points, "Level 1 sollte 5 Skill-Punkte haben");
        assertEquals(14, level10Points, "Level 10 sollte 14 Skill-Punkte haben");
    }

    @Test
    public void testPlayerSkillTreeAverageLevel() {
        playerSkillTree.setAvailableSkillPoints(20);
        playerSkillTree.unlockAbility("water_blast_node", 2);
        playerSkillTree.levelUpAbility("water_blast_node", 1);
        playerSkillTree.unlockAbility("fire_burst_node", 2);

        double avgLevel = playerSkillTree.getAverageAbilityLevel();
        assertEquals(1.5, avgLevel, "Durchschnittliches Level sollte 1.5 sein (2+1)/2");
    }

    @Test
    public void testPlayerSkillTreeExperiencePercent() {
        playerSkillTree.setLevel(1);
        playerSkillTree.setExperience(50);

        int percent = playerSkillTree.getExperiencePercent();
        assertEquals(50, percent, "50 XP von 100 sollte 50% sein");
    }

    @Test
    public void testUnlockedAbilityCount() {
        playerSkillTree.setAvailableSkillPoints(20);
        playerSkillTree.unlockAbility("water_blast_node", 2);
        playerSkillTree.unlockAbility("fire_burst_node", 2);
        playerSkillTree.unlockAbility("ice_spike_node", 2);

        int count = playerSkillTree.getUnlockedAbilityCount();
        assertEquals(3, count, "Sollte 3 freigeschaltete Fähigkeiten haben");
    }

    @Test
    public void testSkillTreeNodeDependencies() {
        SkillTreeNode baseNode = new SkillTreeNode(
                "base_node", "base_ability", "Base", "Base Description",
                1, 2, 1, 5, null, 0, 0
        );

        SkillTreeNode dependentNode = new SkillTreeNode(
                "dependent_node", "dependent_ability", "Dependent", "Dependent Description",
                5, 3, 2, 5, new String[]{"base_node"}, 0, 1
        );

        skillTree.addNode(baseNode);
        skillTree.addNode(dependentNode);

        assertEquals(2, skillTree.getAllNodes().size());
    }

    @Test
    public void testInsufficientSkillPoints() {
        playerSkillTree.setAvailableSkillPoints(1);

        assertFalse(playerSkillTree.unlockAbility("water_blast_node", 2),
                "Sollte nicht freigeschaltet werden können (nicht genug Punkte)");
    }

    @Test
    public void testMaxLevelCap() {
        playerSkillTree.setAvailableSkillPoints(100);
        playerSkillTree.unlockAbility("water_blast_node", 2);

        // Versuche, über Max-Level zu leveln
        for (int i = 0; i < 10; i++) {
            playerSkillTree.levelUpAbility("water_blast_node", 1);
        }

        int level = playerSkillTree.getAbilityLevel("water_blast_node");
        assertTrue(level >= 1 && level <= 5, "Level sollte zwischen 1 und 5 sein, ist aber: " + level);
    }
}
