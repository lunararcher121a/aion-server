package com.aionemu.gameserver.silkroad.skills;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.SchemaFactory;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.dataholders.SkillData;
import com.aionemu.gameserver.skillengine.effect.SkillAttackInstantEffect;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.utils.xml.JAXBUtil;

/** Tests additive XML loading without an actual Aion client or database. */
class BicheonPrototypeTemplateTest {
    private static final File XML = new File("data/static_data/skills/silkroad_bicheon.xml");
    private static final File XSD = new File("data/static_data/skills/skills.xsd");

    @Test
    void prototypeUsesSupportedAionIdAndMatchingSilkroadRank() {
        assertTrue(BicheonPrototype.AION_SKILL_ID > 50047);
        assertTrue(BicheonPrototype.AION_SKILL_ID <= 60000);
        var sro = ChineseSkillCatalog.find(BicheonPrototype.SILKROAD_SKILL_ID).orElseThrow();
        assertEquals(5, sro.requiredMasteryLevel());
        assertEquals(2, sro.skillPointCost());
        assertEquals("SKILL_CH_SWORD_SMASH_A_01", sro.codename());
        assertEquals(40, BicheonPrototype.PROTOTYPE_COOLDOWN);
    }

    @Test
    void bicheonTemplateValidatesAgainstExistingAionSchema() throws Exception {
        assertTrue(XML.isFile(), "The optional template must be packaged in game-server/data");
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(XSD)
                .newValidator()
                .validate(new StreamSource(XML));
    }

    @Test
    void xmlLoadsAsRealSkillEngineTemplateAndCanBeMerged() {
        SkillData addon = JAXBUtil.deserialize(XML, SkillData.class, XSD.getPath());
        var template = addon.getSkillTemplate(BicheonPrototype.AION_SKILL_ID);
        assertNotNull(template);
        assertEquals(SkillType.PHYSICAL, template.getType());
        assertEquals("Silkroad - Bicheon Smash A (Prototype)", template.getName());
        assertNotNull(template.getProperties());
        assertNotNull(template.getStartconditions());
        assertNotNull(template.getEndconditions());
        assertNotNull(template.getEffects());
        assertTrue(template.getEffects().getEffects().get(0) instanceof SkillAttackInstantEffect);
        assertNull(template.getMotion(), "Client animation mapping is deliberately not fabricated");

        SkillData base = new SkillData();
        base.registerAdditionalTemplates(addon);
        assertSame(template, base.getSkillTemplate(BicheonPrototype.AION_SKILL_ID));
        assertThrows(IllegalArgumentException.class, () -> base.registerAdditionalTemplates(addon));
    }

    @Test
    void doesNotCollideWithOriginalAionSkillIds() throws Exception {
        Path vanilla = Path.of("data/static_data/skills/skill_templates.xml");
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        try (InputStream in = Files.newInputStream(vanilla)) {
            XMLStreamReader xml = factory.createXMLStreamReader(in);
            try {
                while (xml.hasNext()) {
                    if (xml.next() == XMLStreamConstants.START_ELEMENT
                            && "skill_template".equals(xml.getLocalName())) {
                        String id = xml.getAttributeValue(null, "skill_id");
                        assertNotEquals(String.valueOf(BicheonPrototype.AION_SKILL_ID), id,
                                "Chosen prototype id is already owned by Aion");
                    }
                }
            } finally {
                xml.close();
            }
        }
    }

    @Test
    void disabledOrMissingPlayerNeverGetsImplicitSkillPermission() {
        assertFalse(BicheonPrototype.hasSword(null));
        assertFalse(BicheonPrototype.canCast(null));
    }
}
