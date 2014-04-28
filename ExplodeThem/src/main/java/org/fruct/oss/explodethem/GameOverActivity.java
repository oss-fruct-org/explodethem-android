package org.fruct.oss.explodethem;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class GameOverActivity extends Activity implements View.OnClickListener {
	public static final String EXTRA_NAME = "org.fruct.oss.explodethem.EXTRA_NAME";
	public static final String ARG_SCORE = "org.fruct.oss.explodethem.ARG_SCORE";

	private EditText nameEdit;
	private Button okButton;
	private TextView scoreText;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

		int score = getIntent().getIntExtra(ARG_SCORE, 0);

		okButton = (Button) findViewById(R.id.ok_button);
		nameEdit = (EditText) findViewById(R.id.name_edit);
		scoreText = (TextView) findViewById(R.id.score_text);

		scoreText.setText(scoreText.getText().toString() + score);

		okButton.setOnClickListener(this);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_over, menu);
        return true;
    }

	@Override
	public void onClick(View view) {
		if (view == okButton) {
			Intent data = new Intent();
			String text;
			if (nameEdit.getText() != null) {
				text = nameEdit.getText().toString();
			} else {
				text = "Anonymous";
			}

			data.putExtra(EXTRA_NAME, text);
			setResult(Activity.RESULT_OK, data);
			finish();
		}
	}
}
