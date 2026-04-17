describe('Story Mode', () => {
  it('keeps Story tied to operator routes', () => {
    cy.visit('/story');
    cy.location('pathname').should('match', /^\/(operators|story|register)/);

    cy.visit('/story/operator/op_kira-vale');
    cy.location('pathname').should('match', /^\/(story\/operator\/op_kira-vale|register)/);
  });
});
