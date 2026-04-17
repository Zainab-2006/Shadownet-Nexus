describe('Missions Runtime', () => {
  it('exposes Missions as the squad gameplay route without a Team nav mode', () => {
    cy.visit('/');
    cy.get('nav').contains('Missions').should('be.visible');
    cy.get('nav').should('not.contain', 'Team');

    cy.visit('/missions');
    cy.contains('Mission Chapters').should('be.visible');
    cy.contains('Mission cell mechanics load inside the runtime.').should('be.visible');
  });
});
