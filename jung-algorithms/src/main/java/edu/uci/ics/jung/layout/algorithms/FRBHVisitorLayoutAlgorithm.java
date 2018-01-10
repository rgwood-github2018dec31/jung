/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.layout.algorithms;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.Point;
import edu.uci.ics.jung.layout.spatial.BarnesHutQuadTree;
import edu.uci.ics.jung.layout.spatial.ForceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This subclass of FRLayoutAlgorithm applies a Barnes-Hut QuadTree optimization during the
 * calculation of node repulsion
 *
 * @author Tom Nelson
 */
public class FRBHVisitorLayoutAlgorithm<N> extends FRLayoutAlgorithm<N>
    implements IterativeContext {

  private static final Logger log = LoggerFactory.getLogger(FRBHVisitorLayoutAlgorithm.class);

  private BarnesHutQuadTree<N> tree;

  @Override
  public void visit(LayoutModel<N> layoutModel) {
    super.visit(layoutModel);
    tree = new BarnesHutQuadTree(layoutModel.getWidth(), layoutModel.getHeight());
  }

  @Override
  public synchronized void step() {
    tree.rebuild(layoutModel.getGraph().nodes(), layoutModel);
    super.step();
  }

  protected void calcRepulsion(N node1) {
    Point fvd1 = getFRData(node1);
    if (fvd1 == null) {
      return;
    }
    frNodeData.put(node1, Point.ORIGIN);

    ForceObject<N> nodeForceObject =
        new ForceObject(node1, layoutModel.apply(node1)) {
          @Override
          protected void addForceFrom(ForceObject other) {
            double dx = this.p.x - other.p.x;
            double dy = this.p.y - other.p.y;
            log.trace("dx, dy:{},{}", dx, dy);
            double dist = Math.sqrt(dx * dx + dy * dy);
            dist = Math.max(EPSILON, dist);
            log.trace("dist:{}", dist);
            double force = (repulsion_constant * repulsion_constant) / dist;
            log.trace("force:{}", force);
            f = f.add(force * (dx / dist), force * (dy / dist));
          }
        };
    tree.visit(nodeForceObject);
    frNodeData.put(node1, nodeForceObject.f);
  }
}
