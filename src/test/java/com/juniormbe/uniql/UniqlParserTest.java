package com.juniormbe.uniql;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The UniqlParserTest class
 *
 * @author Junior Mbe
 * @version 1.0
 * @since 28/06/2019
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("UniqlParserTest Should All Succed")
class UniqlParserTest {
    @Test
    @DisplayName("Uniql Object format as string should succed")
    void UniqlObjectFormat_Should_Succed() {
        Uniql uniql = Uniql.build("category")
          .addField("name")
          .addField("description")
          .addField(Uniql.build("subCategories")
            .addField("name")
            .addField("description")
          )
          .addField(Uniql.build("products")
            .addField("name")
            .addField("description")
            .setPage(PageRequest.of(13, 100))
            .setSort(SortRequest.of(Direction.ASC, new String[] {"name", "unitPrice"}))
          )
          .setQuery("setName=!=skdm;description==samsu%");

        String model = uniql.toModel();
        String fModel = uniql.toFormattedModel();

        System.out.println(model);
        System.out.println(fModel);

        assertDoesNotThrow(()-> {
            Uniql parsed = UniqlParser.parse(model);
            System.out.println(parsed.toFormattedModel());
        });
    }
    @Test
    @DisplayName("Uniql string model parsing to object should succed")
    void UniqlParse_Should_Succed() {
        assertDoesNotThrow(() -> {
            Uniql parsed = UniqlParser.parse("product{setName, description, category{setName, tag{setName}|search||}}");
            System.out.println(parsed.toFormattedModel());
        });
    }
}
