describe('Solo Mode', () => {
  it('shows ranked, teaching, and coaching modes', () => {
    cy.visit('/solo');
    cy.contains('Solo').should('be.visible');
    cy.contains('Ranked Solve').should('be.visible');
    cy.contains('Teaching Mode').should('be.visible');
    cy.contains('Coaching Mode').should('be.visible');
  });
});
