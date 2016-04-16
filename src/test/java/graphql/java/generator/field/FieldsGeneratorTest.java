package graphql.java.generator.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import graphql.java.generator.BuildContext;
import graphql.java.generator.RecursiveClass;
import graphql.java.generator.BuildContext.Builder;
import graphql.java.generator.field.reflect.FieldDataFetcher_Reflection;
import graphql.java.generator.field.reflect.FieldName_Reflection;
import graphql.java.generator.field.reflect.FieldObjects_Reflection;
import graphql.java.generator.field.reflect.FieldObjects_ReflectionClassFields;
import graphql.java.generator.field.reflect.FieldObjects_ReflectionClassMethods;
import graphql.java.generator.field.reflect.FieldType_Reflection;
import graphql.java.generator.type.TypeRepository;
import graphql.schema.GraphQLFieldDefinition;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
@RunWith(Parameterized.class)
public class FieldsGeneratorTest {
    private static Logger logger = LoggerFactory.getLogger(
            FieldsGeneratorTest.class);
    
    FieldsGenerator generator;
    BuildContext testContext;
    
    public FieldsGeneratorTest(FieldsGenerator fieldsGen) {
        generator = fieldsGen;
        testContext = new Builder()
                .setTypeGeneratorStrategy(BuildContext.defaultTypeGenerator)
                .setFieldsGeneratorStrategy(fieldsGen)
                .usingTypeRepository(true)
                .build();
    }
    @Before
    public void before() {
        TypeRepository.clear();
    }
    
    @Parameters
    public static Collection<Object[]> data() {
        final FieldsGenerator fieldsByJavaMethods = new FieldsGenerator(
                new FieldStrategies.Builder()
                        .fieldObjectsStrategy(new FieldObjects_ReflectionClassMethods())
                        .fieldNameStrategy(new FieldName_Reflection())
                        .fieldTypeStrategy(new FieldType_Reflection())
                        .fieldDataFetcherStrategy(new FieldDataFetcher_Reflection())
                        .build());
        final FieldsGenerator fieldsByJavaFields = new FieldsGenerator(
                new FieldStrategies.Builder()
                        .fieldObjectsStrategy(new FieldObjects_ReflectionClassFields())
                        .fieldNameStrategy(new FieldName_Reflection())
                        .fieldTypeStrategy(new FieldType_Reflection())
                        .fieldDataFetcherStrategy(new FieldDataFetcher_Reflection())
                        .build());
        final FieldsGenerator fieldsCombined = new FieldsGenerator(
                new FieldStrategies.Builder()
                        .fieldObjectsStrategy(new FieldObjects_Reflection())
                        .fieldNameStrategy(new FieldName_Reflection())
                        .fieldTypeStrategy(new FieldType_Reflection())
                        .fieldDataFetcherStrategy(new FieldDataFetcher_Reflection())
                        .build());
        @SuppressWarnings("serial")
        ArrayList<Object[]> list = new ArrayList<Object[]>() {{
            add(new Object[] {fieldsByJavaMethods});
            add(new Object[] {fieldsByJavaFields});
            add(new Object[] {fieldsCombined});
        }};
        return list;
    }

    @Test
    public void testRecursion() {
        logger.debug("testRecursion");
        Object object = generator.getFields(RecursiveClass.class, testContext);
        Assert.assertThat(object, instanceOf(List.class));
        List<GraphQLFieldDefinition> recursiveFields = (List<GraphQLFieldDefinition>) object;
        
        Matcher<Iterable<GraphQLFieldDefinition>> hasItemsMatcher =
                hasItems(
                        hasProperty("name", is("recursionLevel")),
                        hasProperty("name", is("recursive")));
        assertThat(recursiveFields, hasItemsMatcher);
        assertThat(recursiveFields.size(), is(2));
    }
}
