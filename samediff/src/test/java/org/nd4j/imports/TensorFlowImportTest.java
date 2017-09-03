package org.nd4j.imports;

import com.google.protobuf.TextFormat;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.nd4j.autodiff.opstate.OpExecAction;
import org.nd4j.autodiff.opstate.OpState;
import org.nd4j.autodiff.samediff.SDGraph;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.autodiff.samediff.impl.SDVariable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.util.HashUtil;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


@Slf4j
public class TensorFlowImportTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testHashEquality1() {
        long hash = HashUtil.getLongHash("Conv2D");
        assertEquals(-1637140380760460323L, hash);
    }


    @Test
    public void testHashEquality2() {
        long hash = HashUtil.getLongHash("switch");
        assertEquals(-1988317239813741487L, hash);
    }

    @Test
    public void testTile1() {
        INDArray array = Nd4j.create(new double[] {1,2,3,4,5,6}, new int[]{1, 2,3});
        INDArray tile = Nd4j.tile(array, 5, 6, 3);

        log.info("New shape: {}", Arrays.toString(tile.shapeInfoDataBuffer().asInt()));
        log.info("Data: {}", tile);
    }

    @Test
    public void testCustomOps1() {
        val map = Nd4j.getExecutioner().getCustomOperations();

        assertTrue(map.size() > 0);

        for (val key: map.keySet()) {
            log.info("OpName: {}; NumInputs: {}; NumOutputs: {}", key, map.get(key).getFirst(), map.get(key).getSecond());
        }
    }

    @Test
    public void importGraph1() throws Exception {
        SameDiff graph = TensorFlowImport.importGraph(new ClassPathResource("tf_graphs/max_add_2.pb.txt").getFile());

        assertNotNull(graph);

        assertEquals(2, graph.getSameDiffVariables().size());
        assertEquals(2, graph.getGraph().getInputs().size());
        assertEquals(1, graph.getGraph().getOpOrder().getActions().size());

        List<OpExecAction> actions = graph.getGraph().getOpOrder().getActions();
        assertEquals(1, actions.size());

        OpState state = actions.get(0).getOpState();

        assertEquals(OpState.OpType.TRANSFORM, state.getOpType());
        assertEquals(0, state.getOpNum());

        SDVariable var0 = graph.getVariableMap().get("zeros");
        SDVariable var1 = graph.getVariableMap().get("ones");

        assertNotNull(var0);
        assertNotNull(var1);

        assertNotNull(var0.getArr());
        assertNotNull(var1.getArr());

        assertEquals(0.0, var0.getArr().sumNumber().doubleValue(), 1e-5);
        assertEquals(12.0, var1.getArr().sumNumber().doubleValue(), 1e-5);
    }


    @Test
    public void importGraph2() throws Exception {
        SameDiff graph = TensorFlowImport.importGraph(new ClassPathResource("tf_graphs/tensorflow_inception_graph.pb").getFile());

        assertNotNull(graph);
    }


    @Test
    public void importGraph3() throws Exception {
        SameDiff graph = TensorFlowImport.importGraph(new ClassPathResource("tf_graphs/max_log_reg.pb.txt").getFile());

        assertNotNull(graph);
    }
}