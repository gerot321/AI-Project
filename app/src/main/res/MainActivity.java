public class MainActivity extends Activity  {


    private DrawView drawView;
    private PointF mTmpPiont = new PointF();
    private float mLastY;
    private Button clear, classify;
    private DrawModel drawModel;

    private static final int WIDTH = 28;
    private TextView resText;

    private float mLastX;
    private List<Classifier> mClassifiers = new ArrayList<>();
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        clear = (Button) findViewById(R.id.btn_clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawModel.clear();
                drawView.reset();
                drawView.invalidate();

                resText.setText("");
            }
        });
        drawView = (DrawView) findViewById(R.id.draw);
        drawModel = new DrawModel(WIDTH, WIDTH);
        drawView.setModel(drawModel);

        drawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction() & MotionEvent.ACTION_MASK;



                if (action == MotionEvent.ACTION_DOWN) {

                    mLastX = event.getX();
                    mLastY = event.getY();

                    drawView.calcPos(mLastX, mLastY, mTmpPiont);

                    float lastConvX = mTmpPiont.x;
                    float lastConvY = mTmpPiont.y;

                    drawModel.startLine(lastConvX, lastConvY);
                    return true;

                } else if (action == MotionEvent.ACTION_MOVE) {
                    float x = event.getX();
                    float y = event.getY();

                    drawView.calcPos(x, y, mTmpPiont);
                    float newConvX = mTmpPiont.x;
                    float newConvY = mTmpPiont.y;
                    drawModel.addLineElem(newConvX, newConvY);

                    mLastX = x;
                    mLastY = y;
                    drawView.invalidate();
                    return true;

                } else if (action == MotionEvent.ACTION_UP) {
                    drawModel.endLine();
                    return true;
                }
                return false;
            }
        });

        resText = (TextView) findViewById(R.id.tfRes);


        classify = (Button) findViewById(R.id.btn_class);
        classify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                float pixels[] = drawView.getPixelData();


                String text = "";

                for (Classifier classifier : mClassifiers) {

                    final Classification res = classifier.recognize(pixels);

                    if (res.getLabel() == null) {
                        text += classifier.name() + ": ?\n";
                    } else {

                        text += String.format("%s %s\n%s %f\n", "Result : ", res.getLabel(),"Confidence : ",
                                res.getConf());
                    }
                }
                resText.setText(text);
            }
        });


        loadModel();
    }


    @Override

    protected void onPause() {
        drawView.onPause();
        super.onPause();
    }
    @Override

    protected void onResume() {
        drawView.onResume();
        super.onResume();
    }



    private void loadModel() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    mClassifiers.add(
                            TensorFlowClassifier.create(getAssets(), "TensorFlow",
                                    "train_model.pb", "labels.txt", WIDTH,
                                    "input", "output", true));

                } catch (final Exception e) {

                    throw new RuntimeException("Error initializing classifiers!", e);
                }
            }
        }).start();
    }






