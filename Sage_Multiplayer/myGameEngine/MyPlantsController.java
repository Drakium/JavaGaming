package myGameEngine;

import graphicslib3D.Matrix3D;
import sage.scene.Controller;
import sage.scene.SceneNode;

//Plant Controller for growing plants 
public class MyPlantsController extends Controller {

	private double growRate = 1;
	private double cycleTime = 5.0; 
	private double totalTime;
	private double direction = 1.0;
	
	@Override
	public void update(double time) { 
		totalTime += (time / (1000000000 / 1));
		//System.out.println(totalTime);
		if(totalTime > cycleTime){
			totalTime = 0.0;
		}
		float transAmount = (float) (growRate + totalTime);

		transAmount = (float) (direction * transAmount);
		
		Matrix3D newTrans = new Matrix3D();
		newTrans.scale(transAmount, transAmount, transAmount);
		
		for (SceneNode node : controlledNodes) {
			Matrix3D curTrans = node.getLocalScale();
			curTrans.concatenate(newTrans);
			node.setLocalTranslation(curTrans);
		}
	}
	
}
