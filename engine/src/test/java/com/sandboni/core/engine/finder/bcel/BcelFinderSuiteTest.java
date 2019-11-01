package com.sandboni.core.engine.finder.bcel;

import com.sandboni.core.engine.FinderTestBase;
import com.sandboni.core.engine.contract.ChangeDetector;
import com.sandboni.core.engine.contract.Finder;
import com.sandboni.core.engine.finder.bcel.visitors.AffectedClassVisitor;
import com.sandboni.core.engine.finder.bcel.visitors.TestClassVisitor;
import com.sandboni.core.engine.sta.graph.Link;
import com.sandboni.core.engine.sta.graph.LinkType;
import com.sandboni.core.engine.sta.graph.vertex.TestVertex;
import com.sandboni.core.scm.scope.*;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.sandboni.core.engine.MockChangeDetector.PACKAGE_NAME;
import static com.sandboni.core.engine.sta.graph.vertex.VertexInitTypes.START_VERTEX;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BcelFinderSuiteTest extends FinderTestBase {

    private static final String TEST_LOCATION = "./target/test-classes/com/sandboni/core/engine/scenario/suite";
    private static final String TEST_PACKAGE = "com.sandboni.core.engine.scenario.suite";

    public BcelFinderSuiteTest() {
        super(TEST_LOCATION, TEST_PACKAGE);
    }

    @Before
    public void setUp() {
        super.initializeContext(new MockForSuiteChangeDetector().getChanges("1", "2"));
    }

    private void testVisitor(Link[] expectedLinks, ClassVisitor... visitors) {
        Finder f = new BcelFinder(visitors);
        f.findSafe(context);

        assertLinksExist(expectedLinks);
    }

    private void testTestClassVisitor(Link... expectedLinks) {
        testVisitor(expectedLinks, new TestClassVisitor());
    }

    @Test
    public void testTestSuiteIsDetected() {
        Link expectedLink1 = newLink(START_VERTEX, new TestVertex.Builder(TEST_PACKAGE + ".SuiteTestClass1", "print()",null).withIncluded(true).build(), LinkType.ENTRY_POINT);
        Link expectedLink2 = newLink(START_VERTEX, new TestVertex.Builder(TEST_PACKAGE + ".SuiteTestClass2", "print()",null).withIncluded(true).build(), LinkType.ENTRY_POINT);
        Link expectedLink3 = newLink(START_VERTEX, new TestVertex.Builder(TEST_PACKAGE + ".SuiteTestClass3", "print()",null).withIncluded(false).build(), LinkType.ENTRY_POINT);

        testTestClassVisitor(expectedLink1, expectedLink2, expectedLink3);
    }

    private static class MockForSuiteChangeDetector implements ChangeDetector {

        @Override
        public ChangeScope<Change> getChanges(String fromChangeId, String toChangeId) {
            ChangeScope<Change> changeScope = new ChangeScopeImpl();
            changeScope.addChange(new SCMChange(TEST_PACKAGE.replace('.', '/') + "/SuiteTestClass1.java",
                    IntStream.range(8, 10).boxed().collect(Collectors.toSet()), ChangeType.MODIFY));
            return changeScope;
        }
    }

}
