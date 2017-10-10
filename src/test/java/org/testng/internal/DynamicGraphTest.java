package org.testng.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.DynamicGraph.Status;
import org.testng.xml.XmlSuite;
import test.SimpleBaseTest;
import test.TestClassContainerForGitHubIssue1360;

public class DynamicGraphTest extends SimpleBaseTest {

  private static class Node {
    private final String name;

    private Node(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  private static void assertFreeNodesEquals(DynamicGraph<Node> graph, Node... expected) {
    assertThat(graph.getFreeNodes()).containsOnly(expected);
  }

  @Test
  public void test8() {
    /*
      digraph test8 {
        a1;
        a2;
        b1 -> {a1; a2;}
        b2 -> {a1; a2;}
        c1 -> {b1; b2;}
        x;
        y;
      }
    */
    DynamicGraph<Node> dg = new DynamicGraph<>();
    Node a1 = new Node("a1");
    Node a2 = new Node("a2");
    Node b1 = new Node("b1");
    Node b2 = new Node("b2");
    Node c1 = new Node("c1");
    dg.addNode(a1);
    dg.addNode(a2);
    dg.addNode(b1);
    dg.addNode(b2);
    dg.addNode(c1);
    dg.addEdge(1, b1, a1, a2);
    dg.addEdge(1, b2, a1, a2);
    dg.addEdge(1, c1, b1, b2);
    dg.addEdge(0, a2, a1, b1, c1);
    dg.addEdge(0, b2, a1, b1, c1);
    Node x = new Node("x");
    Node y = new Node("y");
    dg.addNode(x);
    dg.addNode(y);
    dg.addEdge(0, a1, x, y);
    dg.addEdge(0, b1, x, y);
    dg.addEdge(0, c1, x, y);

    assertFreeNodesEquals(dg, y, x);

    dg.setStatus(dg.getFreeNodes(), Status.RUNNING);
    assertFreeNodesEquals(dg);
    dg.setStatus(x, Status.FINISHED);
    dg.setStatus(y, Status.FINISHED);
    assertFreeNodesEquals(dg, a1);
    
    dg.setStatus(a1, Status.RUNNING);
    assertFreeNodesEquals(dg);
    dg.setStatus(a1, Status.FINISHED);
    assertFreeNodesEquals(dg, a2);

    dg.setStatus(a2, Status.RUNNING);
    assertFreeNodesEquals(dg);
    dg.setStatus(a2, Status.FINISHED);
    assertFreeNodesEquals(dg, b1);

    dg.setStatus(b1, Status.RUNNING);
    assertFreeNodesEquals(dg);
    dg.setStatus(b1, Status.FINISHED);
    assertFreeNodesEquals(dg, b2);

    dg.setStatus(b2, Status.RUNNING);
    assertFreeNodesEquals(dg);
    dg.setStatus(b2, Status.FINISHED);
    assertFreeNodesEquals(dg, c1);
  }

  @Test
  public void test2() {
    /*
      digraph test2 {
        a1;
        a2;
        b1 -> {a1; a2;}
        x;
      }
    */
    DynamicGraph<Node> dg = new DynamicGraph<>();
    Node a1 = new Node("a1");
    Node a2 = new Node("a2");
    Node b1 = new Node("b1");
    dg.addNode(a1);
    dg.addNode(a2);
    dg.addNode(b1);
    dg.addEdge(1, b1, a1, a2);
    dg.addEdge(0, a2, a1, b1);
    Node x = new Node("x");
    dg.addNode(x);
    dg.addEdge(0, a1, x);
    dg.addEdge(0, b1, x);

    assertFreeNodesEquals(dg, x);

    dg.setStatus(x, Status.RUNNING);
    assertFreeNodesEquals(dg);
    dg.setStatus(x, Status.FINISHED);
    assertFreeNodesEquals(dg, a1);
    
    dg.setStatus(a1, Status.RUNNING);
    assertFreeNodesEquals(dg);
    dg.setStatus(a1, Status.FINISHED);
    assertFreeNodesEquals(dg, a2);

    dg.setStatus(a2, Status.RUNNING);
    assertFreeNodesEquals(dg);
    dg.setStatus(a2, Status.FINISHED);
    assertFreeNodesEquals(dg, b1);

    Node b2 = new Node("b2"); // 2
    dg.setStatus(b2, Status.RUNNING);
    dg.setStatus(b1, Status.FINISHED);
    assertFreeNodesEquals(dg);
  }

  @Test
  public void test3() {
          DynamicGraph<Node> dg = new DynamicGraph<>();
          Node a = new Node("a");
          Node b = new Node("b");
          Node c = new Node("c");
          dg.addNode(a);
          dg.addNode(b);
          dg.addNode(c);
          dg.addEdge(1, a, b);
          dg.addEdge(0, c, b);
          dg.addEdge(0, b, a);

          assertFreeNodesEquals(dg, b);

          dg.setStatus(b, Status.RUNNING);
          assertFreeNodesEquals(dg);
          dg.setStatus(b, Status.FINISHED);
          assertFreeNodesEquals(dg, a);

          dg.setStatus(a, Status.RUNNING);
          assertFreeNodesEquals(dg);
          dg.setStatus(a, Status.FINISHED);
          assertFreeNodesEquals(dg, c);

          dg.setStatus(c, Status.RUNNING);
          assertFreeNodesEquals(dg);
          dg.setStatus(c, Status.FINISHED);
          assertFreeNodesEquals(dg);
  }

  @Test
  public void test4() {
          DynamicGraph<Node> dg = new DynamicGraph<>();
          Node a = new Node("a");
          Node b = new Node("b");
          dg.addNode(a);
          dg.addNode(b);
          dg.addEdge(0, b, a);

          assertFreeNodesEquals(dg, a);

          dg.setStatus(a, Status.RUNNING);
          assertFreeNodesEquals(dg);

          dg.setStatus(a, Status.FINISHED);
          assertFreeNodesEquals(dg, b);

          dg.setStatus(b, Status.RUNNING);
          assertFreeNodesEquals(dg);

          dg.setStatus(b, Status.FINISHED);
          assertFreeNodesEquals(dg);
  }
  @Test
  public void testOrderingOfEdgesWithSameWeight() {
    Class<?>[] classes = new Class[] {
        TestClassContainerForGitHubIssue1360.TestNG1.class,
        TestClassContainerForGitHubIssue1360.TestNG2.class,
        TestClassContainerForGitHubIssue1360.TestNG3.class
    };
    List<ITestNGMethod> methods = extractTestNGMethods(classes);
    DynamicGraph<ITestNGMethod> graph = new DynamicGraph<>();

    ListMultiMap<Integer, ITestNGMethod> methodsByPriority = Maps.newListMultiMap();
    for (ITestNGMethod method : methods) {
      methodsByPriority.put(method.getPriority(), method);
      graph.addNode(method);
    }
    List<Integer> availablePriorities = Lists.newArrayList(methodsByPriority.keySet());
    Collections.sort(availablePriorities);
    Integer previousPriority = methods.size() > 0 ? availablePriorities.get(0) : 0;
    for (int i = 1; i < availablePriorities.size(); i++) {
      Integer currentPriority = availablePriorities.get(i);
      for (ITestNGMethod p0Method : methodsByPriority.get(previousPriority)) {
        for (ITestNGMethod p1Method : methodsByPriority.get(currentPriority)) {
          graph.addEdge(1, p1Method, p0Method);
        }
      }
      previousPriority = currentPriority;
    }
    List<String> expected = Arrays.asList("TestNG1.test1TestNG1", "TestNG2.test1TestNG2", "TestNG3.test1TestNG3");
    runAssertion(graph, expected);
    expected = Arrays.asList("TestNG1.test2TestNG1", "TestNG2.test2TestNG2", "TestNG3.test2TestNG3");
    runAssertion(graph, expected);

    expected = Arrays.asList("TestNG1.test3TestNG1", "TestNG2.test3TestNG2", "TestNG3.test3TestNG3");
    runAssertion(graph, expected);
  }

  private static void runAssertion(DynamicGraph<ITestNGMethod> graph, List<String> expected) {
    List<ITestNGMethod> p1Methods = graph.getFreeNodes();
    Assert.assertEquals(p1Methods.size(), 3);
    graph.setStatus(p1Methods, Status.FINISHED);
    for (ITestNGMethod p1Method : p1Methods) {
      Assert.assertTrue(expected.contains(constructName(p1Method)));
    }
  }

  private static String constructName(ITestNGMethod method) {
    return method.getConstructorOrMethod().getDeclaringClass().getSimpleName() + "." + method.getMethodName();
  }

  @Test
  public void performanceTest() {
      long runtimeUsingLists = executionTime(true);
      long runtimeUsingSets = executionTime(false);
      assertThat(runtimeUsingSets).isLessThan(runtimeUsingLists);
      Reporter.log("Time taken for list backed implementation : " + runtimeUsingLists, true);
      Reporter.log("Time taken for set backed implementation : " + runtimeUsingSets, true);
  }

  @Test
  public void testDuplicationFunctionality() {
      XmlSuite suite = createXmlSuite("suite", "test", TestClassSample.class);
      TestNG testng = create(suite);
      MethodMultiplyingInterceptor tla = new MethodMultiplyingInterceptor();
      testng.addListener((ITestNGListener) tla);
      testng.run();
      int expected = tla.getMultiplyCount() + tla.getOriginalMethodCount();
      assertThat(tla.getPassedTests().size()).isEqualTo(expected);
  }

  private long executionTime(boolean useLists) {
      DynamicGraph<TestNGObject> graph = new DynamicGraph<>(useLists);
      addDummyNodesWithOnlyLastNodeFree(graph);
      TestNGObject node1 = new TestNGObject(UUID.randomUUID().toString());
      TestNGObject node2 = new TestNGObject(UUID.randomUUID().toString());
      graph.addEdge(2, node2, node1);
      long start = System.currentTimeMillis();
      graph.setStatus(node1, Status.FINISHED);
      graph.getFreeNodes();
      long end = System.currentTimeMillis();
      return (end-start);
  }

    private void addDummyNodesWithOnlyLastNodeFree(DynamicGraph<TestNGObject> graph) {
        for (int i = 0; i < 1000; i++) {
            String text = UUID.randomUUID().toString();
            TestNGObject node = new TestNGObject(text);
            graph.addNode(node);
        }
    }

  public static class TestNGObject {
      private String text;
      private int sleepTime;

      TestNGObject(String text) {
          this.text = text;
          this.sleepTime = new Random().nextInt(5);
      }

      private void sleep() {
          try {
              TimeUnit.MILLISECONDS.sleep(sleepTime *10);
          } catch (InterruptedException e) {
              throw new RuntimeException(e);
          }
      }

      @Override
      public boolean equals(Object o) {
          sleep();
          if (this == o) return true;
          if (o == null || getClass() != o.getClass()) return false;

          TestNGObject that = (TestNGObject) o;

          return text != null ? text.equals(that.text) : that.text == null;
      }

      @Override
      public int hashCode() {
          return text != null ? text.hashCode() : 0;
      }
  }
}
